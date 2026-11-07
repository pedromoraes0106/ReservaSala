# ReservaSala

Aplicação de reservas de salas com API Spring Boot protegida por JWT.

## Executar

```bash
./mvnw spring-boot:run
```

No Windows: `mvnw.cmd spring-boot:run`.

A API ficará disponível em `http://localhost:8080`. Documentação Swagger: `http://localhost:8080/api/v1/api-docs`.

## Interface Web

O front-end responsivo está na pasta [`frontend`](frontend/) e é gerenciado com npm e Vite. Também há instruções completas para implantação na Vercel em [`frontend/README.md`](frontend/README.md).

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5500`, configure a API como `http://localhost:8080` e autentique-se ou crie uma conta.

## Permissões

- Qualquer usuário autenticado pode listar, cadastrar, editar e excluir salas.
- Qualquer usuário autenticado pode criar, consultar, cancelar e realizar check-in em reservas.
- Não há diferenciação de acesso por perfil (`USER`/`ADMIN`).
- Um `401` indica token ausente, inválido ou expirado; após o login, autentique-se novamente.

## API de perfil

A rota autenticada `GET /api/v1/me` retorna o usuário e seu perfil, por exemplo:

```json
{"email":"usuario@exemplo.com","role":"USER"}
```
