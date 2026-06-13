import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // gym-service (8082) - trainers, rooms, equipment, classes, enrollments, check-ins
      "/api/trainers": "http://localhost:8082",
      "/api/rooms": "http://localhost:8082",
      "/api/equipment": "http://localhost:8082",
      "/api/gym-classes": "http://localhost:8082",
      "/api/class-enrollments": "http://localhost:8082",
      "/api/check-ins": "http://localhost:8082",
      // notification-service (8083) - reports
      "/api/reports": "http://localhost:8083",
      // user-service (8081) - members, plans, subscriptions, profiles, auth (catch-all)
      "/api": "http://localhost:8081",
      "/login": "http://localhost:8081",
      "/logout": "http://localhost:8081"
    }
  }
});
