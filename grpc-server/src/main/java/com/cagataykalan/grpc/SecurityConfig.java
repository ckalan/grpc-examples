//package com.cagataykalan.grpc;
//
//import java.util.Collections;
//import java.util.List;
//import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
//import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
//import net.devh.boot.grpc.server.security.authentication.SSLContextGrpcAuthenticationReader;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    GrpcAuthenticationReader authenticationReader() {
//        List<GrpcAuthenticationReader> readers =
//                Collections.singletonList(new SSLContextGrpcAuthenticationReader());
//        return new CompositeGrpcAuthenticationReader(readers);
//    }
//}
