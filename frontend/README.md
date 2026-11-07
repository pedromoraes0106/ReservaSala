# ReservaSala — Front-end

Interface Web em HTML, CSS e JavaScript, executada com npm/Vite.

## Configuração

```bash
npm install
npm run dev
```

O front-end fica disponível em `http://localhost:5500`. Na interface, configure a API para `http://localhost:8080`.

## Comandos

- `npm run dev` — servidor de desenvolvimento.
- `npm run build` — build de produção em `dist`.
- `npm run preview` — servidor de prévia da build.

## Permissões

Todos os usuários autenticados podem listar, cadastrar, editar e excluir salas e gerenciar reservas. Não há restrições por perfil.

O painel de respostas mostra método, rota, código HTTP, corpo da resposta e erros. Tokens são ocultados.

## Deploy na Vercel

Configure o diretório raiz como `frontend`, o comando de build como `npm run build` e o diretório de saída como `dist`. Defina a URL pública da API na interface após o deploy.

`401 Unauthorized` indica token ausente, inválido ou expirado. Faça logout e login novamente.
