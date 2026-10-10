package com.travel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//Lombok 提供的日志注解，加在类上会在编译时自动生成一个 SLF4J 标准的日志对象 log，用来替代手写日志对象的代码，简化日志输出。
@Slf4j
@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.amap.key}")
    private String apiKey;

    @Value("${weather.amap.base-url}")
    private String baseUrl;

    // 本地缓存：key=城市名，value=[天气数据, 缓存时间戳]，默认缓存30分钟
    private final ConcurrentHashMap<String, CacheEntry> weatherCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_MS = TimeUnit.MINUTES.toMillis(30);

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 根据城市名获取实时天气
     * @param cityName 城市名
     * @return 数组：[天气状况, 温度, 出行提示]；调用失败返回null
     */
    public String[] getWeatherInfo(String cityName) {
        // 1. 优先查本地缓存
        CacheEntry cached = weatherCache.get(cityName);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_EXPIRE_MS) {
            return cached.data;
        }

        // 2. 缓存未命中，调用第三方接口
        try {
            String url = String.format("%s?city=%s&key=%s&extensions=base",
                    baseUrl, cityName, apiKey);
            // 发起HTTP请求，解析返回JSON
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"1".equals(response.get("status").toString())) {
                log.warn("天气接口调用失败，城市：{}，返回结果：{}", cityName, response);
                return null;
            }

            // 解析实时天气核心字段
            Map<String, Object> lives = ((java.util.List<Map<String, Object>>) response.get("lives")).get(0);
            String weather = lives.get("weather").toString();
            String temperature = lives.get("temperature").toString() + "℃";

            // 生成出行提示
            String travelTip = generateTravelTip(weather, temperature);

            String[] result = new String[]{weather, temperature, travelTip};

            // 3. 结果写入缓存
            weatherCache.put(cityName, new CacheEntry(result, System.currentTimeMillis()));
            return result;

        } catch (Exception e) {
            // 异常降级：仅打印日志，返回null，不中断主业务流程
            log.error("天气接口调用异常，城市：{}", cityName, e);
            return null;
        }
    }

    /**
     * 根据天气、温度生成简易出行提示
     */
    private String generateTravelTip(String weather, String temperatureStr) {
        StringBuilder tip = new StringBuilder();
        int temp = Integer.parseInt(temperatureStr.replace("℃", "").trim());

        // 天气场景提示
        if (weather.contains("雨")) {
            tip.append("有降雨，记得带伞；");
        } else if (weather.contains("雪")) {
            tip.append("有降雪，注意防滑保暖；");
        } else if (weather.contains("霾") || weather.contains("雾")) {
            tip.append("能见度低，出行注意安全；");
        } else if (weather.contains("晴")) {
            tip.append("天气晴好，适合出行；");
        }

        // 温度场景提示
        if (temp >= 30) {
            tip.append("气温较高，注意防晒补水。");
        } else if (temp <= 5) {
            tip.append("气温较低，注意添衣保暖。");
        } else if (temp >= 15 && temp < 25) {
            tip.append("气温舒适，适宜出游。");
        }

        return tip.length() == 0 ? "出行请注意安全。" : tip.toString();
    }

    /**
     * 缓存数据内部类
     */
    private static class CacheEntry {
        String[] data;
        long timestamp;

        CacheEntry(String[] data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
