package io.deffun.asgard.core;

import graphql.GraphqlErrorException;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLAppliedDirectiveArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AuthorizeFieldVisibility implements GraphqlFieldVisibility {
    private final AuthorizationSecurity authorizationSecurity;

    public AuthorizeFieldVisibility(AuthorizationSecurity authorizationSecurity) {
        this.authorizationSecurity = authorizationSecurity;
    }

    @Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
        List<GraphQLFieldDefinition> allowedFields = new ArrayList<>();
        List<GraphQLFieldDefinition> deniedFields = new ArrayList<>();
        for (GraphQLFieldDefinition field : fieldsContainer.getFieldDefinitions()) {
            boolean accessAllowed = checkField(field);
            if (!accessAllowed) {
                allowedFields.add(field);
            } else {
                deniedFields.add(field);
            }
        }
        if (!deniedFields.isEmpty()) {
            throw GraphqlErrorException.newErrorException()
                    .message("Not authorised to access the following fields: [%s].".formatted(deniedFields.stream().map(GraphQLFieldDefinition::getName).collect(Collectors.joining(", "))))
                    .build();
        }
        return allowedFields;
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
        GraphQLFieldDefinition field = fieldsContainer.getFieldDefinition(fieldName);
        if (field == null) {
            return null;
        }
        boolean accessAllowed = checkField(field);
        if (!accessAllowed) {
            throw GraphqlErrorException.newErrorException()
                    .message("Not authorised to access the field '%s'.".formatted(field.getName()))
                    .build();
        }
        return field;
    }

    private boolean checkField(GraphQLFieldDefinition fieldDefinition) {
        GraphQLAppliedDirective directive = fieldDefinition.getAppliedDirective("authorize");
        if (directive != null) {
            GraphQLAppliedDirectiveArgument rolesArgument = directive.getArgument("roles");
            if (rolesArgument != null) {
                Object value = rolesArgument.getValue();
                if (value instanceof List<?> roles) {
                    for (Object v : roles) {
                        if (v instanceof String role) {
                            boolean allowed = switch (role) {
                                case "Authenticated", "Authn",
                                        "IsAuthenticated", "IsAuthn" -> authorizationSecurity.isAuthn();
                                default -> authorizationSecurity.hasRole(role);
                            };
                            if (!allowed) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
