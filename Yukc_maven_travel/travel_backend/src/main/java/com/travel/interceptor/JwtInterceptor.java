package com.travel.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.common.util.JwtUtil;
import com.travel.common.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;//jackson库提供的JSON序列化/反序列化工具

    public JwtInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 1. 放行跨域预检请求
            if ("OPTIONS".equals(request.getMethod())) {
                return true;
            }

            // 2. 从请求头获取 token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                writeErrorResponse(response, ResultCode.UNAUTHORIZED, "请先登录");
                return false;
            }

            // 3. 提取 token 本体
            String token = authHeader.substring(7);

            // 4. 调用你现有的 parseToken 方法解析
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null) {
                writeErrorResponse(response, ResultCode.UNAUTHORIZED, "登录已过期或无效，请重新登录");
                return false;
            }

            // 5. 从 claims 中取出用户信息，存入线程上下文
            Long userId = claims.get("userId", Long.class);
            Integer isAdmin = claims.get("isAdmin", Integer.class);
            UserContext.setUserId(userId);
            UserContext.setIsAdmin(isAdmin);
            return true;
        } catch (Exception e) {
            // token 解析异常等情况，确保 ThreadLocal 被清理，防止线程池复用时串用户
            UserContext.clear();
            writeErrorResponse(response, ResultCode.UNAUTHORIZED, "登录已过期或无效，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束清空上下文，避免内存泄漏
        UserContext.clear();
    }
    private void writeErrorResponse(HttpServletResponse response, ResultCode code, String msg) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try {
            //用 code.getCode() 取出整数状态码，匹配方法参数类型
            Result<?> result = Result.fail(code.getCode(), msg);
            response.getWriter().write(objectMapper.writeValueAsString(result));//关键一行，把Result对象转成json格式
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
