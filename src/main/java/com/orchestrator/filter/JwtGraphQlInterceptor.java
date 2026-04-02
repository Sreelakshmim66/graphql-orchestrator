package com.orchestrator.filter;

import com.orchestrator.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Intercepts every GraphQL HTTP request.
 * If an Authorization: Bearer <token> header is present and valid,
 * the raw token is stored in the GraphQL context under key "jwtToken".
 *
 * Resolvers retrieve it via env.getGraphQlContext().get("jwtToken").
 * Public operations (login/register) simply ignore the missing token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtGraphQlInterceptor implements WebGraphQlInterceptor {

    private final JwtService jwtService;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request,
                                              WebGraphQlInterceptor.Chain chain) {
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isValid(token)) {
                String userId = jwtService.validateAndExtractUserId(token);
                log.debug("Valid JWT for userId={}", userId);
                // Put token into GraphQL context so resolvers can forward it to downstream services
                request.configureExecutionInput((exec, builder) ->
                        builder.graphQLContext(ctx -> {
                            ctx.put("jwtToken", token);
                            ctx.put("authenticatedUserId", userId);
                        }).build()
                );
            } else {
                log.warn("Invalid JWT received — proceeding without auth context");
            }
        }

        return chain.next(request);
    }
}
