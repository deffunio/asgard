package io.deffun.asgard.core;

import graphql.GraphQL;
import graphql.GraphqlErrorException;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeFieldVisibilityTest {
    @Mock
    private AuthorizationSecurity authorizationSecurity;

    private AuthorizeFieldVisibility authorizeFieldVisibility;

    @BeforeEach
    void setUp() {
        authorizeFieldVisibility = new AuthorizeFieldVisibility(authorizationSecurity);
    }

    @Test
    void sampleTest() {
        String gqlSchema = """
                directive @authorize(roles: [String!]) on FIELD_DEFINITION | OBJECT | INTERFACE
                type TestType {
                  testField1: String!
                }
                type Query {
                  testQuery1: TestType! @authorize(roles: ["IsAuthn"])
                  testQuery2: TestType! @authorize(roles: ["SomeRole"])
                }
                """;
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(gqlSchema);
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .fieldVisibility(authorizeFieldVisibility)
                .build();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

        when(authorizationSecurity.isAuthn()).thenReturn(true);

        graphQL.execute("query { testQuery1 { testField1 } }");

        when(authorizationSecurity.hasRole("SomeRole")).thenReturn(false);

        assertThrows(GraphqlErrorException.class, () -> graphQL.execute("query { testQuery2 { testField1 } }"));
    }
}
