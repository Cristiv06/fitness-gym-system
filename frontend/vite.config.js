import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Routing centralizat prin API Gateway (Spring Cloud Gateway, :8080).
      // Gateway-ul ruteaza mai departe catre user-service / gym-service / notification-service.
      "/api": "http://localhost:8080",
      "/login": "http://localhost:8080",
      "/logout": "http://localhost:8080"
    }
  }
});
