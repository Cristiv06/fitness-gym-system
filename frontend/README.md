# Frontend React (Vite)

UI React pentru backend-ul Spring din `backend/`, cu:

- login pe sesiune Spring Security (`/login`)
- logout (`/logout`)
- taburi CRUD pentru toate resursele `/api/**`
- link rapid catre Swagger

## Pornire

1. Porneste backend-ul pe `http://localhost:8080`.
2. Din folderul `frontend`:

```bash
npm install
npm run dev
```

3. Deschide `http://localhost:5173`.

## Note

- Pentru scriere (`POST`, `PUT`, `DELETE`) backend-ul permite doar `ROLE_ADMIN`.
- Daca te loghezi cu `user/User123!`, operatiile de scriere vor intoarce `403` (comportament asteptat).
- Datele pentru formulare sunt configurate central in `src/config/resources.js`.
