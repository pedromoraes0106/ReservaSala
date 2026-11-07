const STORAGE_TOKEN = 'reservasala_token';
const STORAGE_API = 'reservasala_api';

function apiBase() {
  const saved = localStorage.getItem(STORAGE_API);
  if (saved) return saved.replace(/\/$/, '');
  if (location.hostname === 'localhost' || location.hostname === '127.0.0.1') return 'http://localhost:8080';
  return '';
}

function setApiBase(value) {
  localStorage.setItem(STORAGE_API, value.replace(/\/$/, ''));
}

async function api(path, options = {}) {
  const base = apiBase();
  if (!base && location.protocol !== 'file:') throw new Error('Configure o endereço da API antes de continuar.');
  const token = localStorage.getItem(STORAGE_TOKEN);
  const headers = { ...(options.headers || {}) };
  if (!(options.body instanceof FormData)) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = 'Bearer ' + token;
  const method = options.method || 'GET';
  let response;
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), 15000);
  try {
    response = await fetch(`${base}${path}`, { ...options, headers, signal: controller.signal });
  } catch (error) {
    const message = error.name === 'AbortError'
      ? 'A API não respondeu em 15 segundos. Verifique se o servidor está ativo.'
      : `${error.message}. Verifique se a URL da API e o CORS estão configurados corretamente.`;
    showApiResponse(method, path, 0, null, message);
    throw new Error(message);
  } finally {
    window.clearTimeout(timeoutId);
  }

  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (_) { data = text || null; }
  const safeData = redactApiResponse(data);
  showApiResponse(method, path, response.status, safeData, response.ok ? '' : data || `Erro ${response.status}`);

  if (!response.ok) {
    throw new Error(typeof data === 'string' ? data : data?.message || `Erro ${response.status} na requisição.`);
  }
  return response.status === 204 ? null : data;
}

const authView = document.querySelector('#authView');
const dashboardView = document.querySelector('#dashboardView');
const authMessage = document.querySelector('#authMessage');

async function showDashboard() {
  authView.classList.add('hidden');
  dashboardView.classList.remove('hidden');
  document.querySelector('#apiLabel').textContent = apiBase() || 'não configurada';
  loadReservations();

  try {
    await api('/api/v1/me');
    loadRooms();
  } catch (error) {
    document.querySelector('[data-section="rooms"]').classList.add('hidden');
  }
}

function showAuth() {
  dashboardView.classList.add('hidden');
  authView.classList.remove('hidden');
}

function currentSession() {
  if (localStorage.getItem(STORAGE_TOKEN)) showDashboard(); else showAuth();
}

document.querySelectorAll('.tab').forEach(tab => tab.addEventListener('click', () => {
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  tab.classList.add('active');
  document.querySelector('#loginForm').classList.toggle('hidden', tab.dataset.tab !== 'login');
  document.querySelector('#registerForm').classList.toggle('hidden', tab.dataset.tab !== 'register');
  authMessage.textContent = '';
}));

document.querySelector('#loginForm').addEventListener('submit', async event => {
  event.preventDefault();
  authMessage.textContent = '';
  const data = Object.fromEntries(new FormData(event.currentTarget));
  try {
    const result = await api('/api/v1/authenticate', { method: 'POST', body: JSON.stringify(data) });
    localStorage.setItem(STORAGE_TOKEN, result.token);
    showDashboard();
  } catch (error) { authMessage.textContent = error.message; }
});

document.querySelector('#registerForm').addEventListener('submit', async event => {
  event.preventDefault();
  authMessage.textContent = '';
  const data = Object.fromEntries(new FormData(event.currentTarget));
  try {
    await api('/api/v1/register', { method: 'POST', body: JSON.stringify(data) });
    authMessage.style.color = '#15803d';
    authMessage.textContent = 'Conta criada! Entre com seu e-mail e senha.';
    document.querySelector('[data-tab="login"]').click();
    document.querySelector('#loginForm input[name="username"]').value = data.email;
  } catch (error) { authMessage.style.color = '#dc2626'; authMessage.textContent = error.message; }
});

document.querySelector('#logoutButton').addEventListener('click', () => {
  localStorage.removeItem(STORAGE_TOKEN);
  showAuth();
});

document.querySelectorAll('.nav-item').forEach(button => button.addEventListener('click', () => {
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  document.querySelector('#reservationsSection').classList.toggle('hidden', button.dataset.section !== 'reservations');
  document.querySelector('#roomsSection').classList.toggle('hidden', button.dataset.section !== 'rooms');
  document.querySelector('#pageTitle').textContent = button.dataset.section === 'reservations' ? 'Reservas' : 'Salas';
}));

function formatDate(value) {
  return value ? new Date(value).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—';
}

async function loadReservations() {
  const container = document.querySelector('#reservationsList');
  container.innerHTML = '<p class="empty">Carregando...</p>';
  try {
    const reservations = await api('/api/reservas');
    container.innerHTML = reservations.length ? reservations.map(item => `
      <article class="card"><div class="card-top"><div><h3>${escapeHtml(item.solicitante)}</h3><p>${escapeHtml(item.periodo?.inicio ? formatDate(item.periodo.inicio) : '')}</p></div><span class="badge ${item.status === 'CANCELADA' ? 'cancelled' : ''}">${escapeHtml(item.status)}</span></div>
      <p>Até ${escapeHtml(formatDate(item.periodo?.fim))}</p>
      <p>Sala: ${escapeHtml(item.salaId)}</p>
      <div class="card-actions"><button data-cancel-reservation="${item.id}" ${item.status === 'CANCELADA' ? 'disabled' : ''}>Cancelar</button><button data-checkin-reservation="${item.id}" ${item.status === 'CANCELADA' ? 'disabled' : ''}>Check-in</button></div></article>`).join('') : '<p class="empty">Nenhuma reserva encontrada.</p>';
    container.querySelectorAll('[data-cancel-reservation]').forEach(button => button.addEventListener('click', () => cancelReservation(button.dataset.cancelReservation)));
    container.querySelectorAll('[data-checkin-reservation]').forEach(button => button.addEventListener('click', () => checkInReservation(button.dataset.checkinReservation)));
  } catch (error) { container.innerHTML = `<p class="empty">${escapeHtml(error.message)}</p>`; }
}

async function loadRooms() {
  const container = document.querySelector('#roomsList');
  try {
    const rooms = await api('/api/salas');
    container.innerHTML = rooms.length ? rooms.map(room => `
      <article class="card"><div class="card-top"><div><h3>${escapeHtml(room.nome)}</h3><p>Capacidade: ${room.capacidade} pessoas</p></div></div><p>ID: ${room.id}</p><div class="card-actions"><button class="danger" data-delete-room="${room.id}">Excluir</button></div></article>`).join('') : '<p class="empty">Nenhuma sala cadastrada.</p>';
    container.querySelectorAll('[data-delete-room]').forEach(button => button.addEventListener('click', () => deleteRoom(button.dataset.deleteRoom)));
    const select = document.querySelector('#reservationForm select[name="salaId"]');
    select.innerHTML = rooms.map(room => `<option value="${room.id}">${escapeHtml(room.nome)} (${room.capacidade})</option>`).join('');
    return true;
  } catch (error) { container.innerHTML = `<p class="empty">${escapeHtml(error.message)}</p>`; return false; }
}

async function cancelReservation(id) {
  if (!confirm('Cancelar esta reserva?')) return;
  try { await api(`/api/reservas/${id}/cancelamento`, { method: 'POST', body: '{}' }); loadReservations(); } catch (error) { alert(error.message); }
}
async function checkInReservation(id) {
  try { await api(`/api/reservas/${id}/check-in`, { method: 'POST', body: '{}' }); loadReservations(); } catch (error) { alert(error.message); }
}
async function deleteRoom(id) {
  if (!confirm('Excluir esta sala?')) return;
  try { await api(`/api/salas/${id}`, { method: 'DELETE' }); loadRooms(); } catch (error) { alert(error.message); }
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char]);
}

function redactApiResponse(value) {
  if (!value || typeof value !== 'object') return value;
  if (Array.isArray(value)) return value.map(redactApiResponse);
  return Object.fromEntries(Object.entries(value).map(([key, item]) => {
    return [key, /token/i.test(key) && typeof item === 'string' ? `${item.slice(0, 12)}… [redigido]` : redactApiResponse(item)];
  }));
}

function showApiResponse(method, path, status, data, errorMessage) {
  const successful = status >= 200 && status < 300;
  const statusElement = document.querySelector('#apiResponseStatus');
  statusElement.textContent = status ? `${status}` : 'ERRO';
  statusElement.className = `api-response-status ${successful ? 'success' : 'error'}`;
  document.querySelector('#apiResponseBody').textContent = JSON.stringify({
    request: `${method} ${path}`,
    status,
    response: data ?? null,
    ...(errorMessage ? { error: errorMessage } : {})
  }, null, 2);
}

function closeDialog(id) {
  document.querySelector(`#${id}`)?.close();
}

document.querySelectorAll('[data-close-dialog]').forEach(button => {
  button.addEventListener('click', () => closeDialog(button.dataset.closeDialog));
});

document.querySelectorAll('dialog').forEach(dialog => {
  dialog.addEventListener('click', event => {
    const bounds = dialog.getBoundingClientRect();
    const outside = event.clientX < bounds.left || event.clientX > bounds.right || event.clientY < bounds.top || event.clientY > bounds.bottom;
    if (outside) dialog.close();
  });
});

document.querySelector('#apiResponseToggle').addEventListener('click', () => {
  const content = document.querySelector('#apiResponseContent');
  const hidden = content.classList.toggle('hidden');
  document.querySelector('#apiResponseToggle').setAttribute('aria-expanded', String(!hidden));
});
document.querySelector('#apiResponseClear').addEventListener('click', () => {
  document.querySelector('#apiResponseBody').textContent = 'Nenhuma requisição realizada.';
  document.querySelector('#apiResponseStatus').textContent = 'Aguardando';
  document.querySelector('#apiResponseStatus').className = 'api-response-status';
});

function openApiSettings() {
  document.querySelector('#apiUrl').value = apiBase();
  document.querySelector('#apiModal').showModal();
}
document.querySelector('#apiSettingsButton').addEventListener('click', openApiSettings);
document.querySelector('#apiSettingsButtonDashboard').addEventListener('click', openApiSettings);
document.querySelector('#apiForm').addEventListener('submit', event => {
  event.preventDefault(); setApiBase(document.querySelector('#apiUrl').value.trim()); document.querySelector('#apiModal').close(); document.querySelector('#apiLabel').textContent = apiBase() || 'não configurada'; location.reload();
});

document.querySelector('#newReservationButton').addEventListener('click', async () => {
  const roomsLoaded = await loadRooms();
  if (!roomsLoaded) return;
  const select = document.querySelector('#reservationForm select[name="salaId"]');
  if (!select.options.length) {
    alert('Cadastre pelo menos uma sala antes de criar uma reserva.');
    return;
  }
  document.querySelector('#reservationModal [data-modal-message]').textContent = '';
  document.querySelector('#reservationModal').showModal();
});
document.querySelector('#reservationForm').addEventListener('submit', async event => {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const payload = Object.fromEntries(form);
  payload.inicio = payload.inicio.length === 16 ? `${payload.inicio}:00` : payload.inicio;
  payload.fim = payload.fim.length === 16 ? `${payload.fim}:00` : payload.fim;
  try {
    await api('/api/reservas', { method: 'POST', body: JSON.stringify(payload) });
    document.querySelector('#reservationModal').close();
    event.currentTarget.reset();
    await Promise.all([loadReservations(), loadRooms()]);
  } catch (error) {
    document.querySelector('#reservationModal [data-modal-message]').textContent = error.message;
  }
});

document.querySelector('#newRoomButton').addEventListener('click', () => {
  document.querySelector('#roomModal [data-modal-message]').textContent = '';
  document.querySelector('#roomModal').showModal();
});
document.querySelector('#roomForm').addEventListener('submit', async event => {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.currentTarget));
  payload.capacidade = Number(payload.capacidade);
  try {
    await api('/api/salas', { method: 'POST', body: JSON.stringify(payload) });
    document.querySelector('#roomModal').close();
    event.currentTarget.reset();
    await loadRooms();
  } catch (error) { document.querySelector('#roomModal [data-modal-message]').textContent = error.message; }
});

currentSession();
