package com.varad.productmanagement.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Product Management REST API")
                                                .version("1.0.0")
                                                .description("### 🚀 Evaluator Quick Start Guide\n\n" +
                                                                "**Pre-Seeded Admin Credentials:**\n" +
                                                                "- **Username:** `varad`\n" +
                                                                "- **Password:** `varad@123`\n\n" +
                                                                "---\n\n" +
                                                                "### 🔑 How to Test Protected Endpoints (Add Product / Add Item):\n\n"
                                                                +
                                                                "1. **Login as Admin:** Go to `POST /api/v1/auth/login` → Click **Try it out** → Execute with `{\"username\": \"varad\", \"password\": \"varad@123\"}`.\n"
                                                                +
                                                                "2. **Copy Access Token:** Copy the `accessToken` string from the JSON response.\n"
                                                                +
                                                                "3. **Authorize Swagger:** Click the green **Authorize 🔓** button at the top right → Paste the token → Click **Authorize**.\n"
                                                                +
                                                                "4. **Add Product (ADMIN Only):** Go to `POST /api/v1/products` → Execute with `{\"productName\": \"Laptop\"}` (Returns `201 Created`).\n"
                                                                +
                                                                "5. **Add Item (ADMIN Only):** Go to `POST /api/v1/products/1/items` → Execute with `{\"quantity\": 50}` (Returns `201 Created`).\n"
                                                                +
                                                                "6. **View Products & Items:** Go to `GET /api/v1/products` or `GET /api/v1/products/1/items` (Accessible by `USER` and `ADMIN`).")
                                                .contact(new Contact().name("Varad").email("varadsure362@gmail.com")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}
