const tokenKey = 'accessToken';
const userKey = 'loginUser';

function getToken() { return localStorage.getItem(tokenKey); }
function setLoginUser(data) { localStorage.setItem(tokenKey, data.token); localStorage.setItem(userKey, JSON.stringify(data)); }
function getLoginUser() { const raw = localStorage.getItem(userKey); return raw ? JSON.parse(raw) : null; }
function logout() { localStorage.removeItem(tokenKey); localStorage.removeItem(userKey); location.href = '/'; }

async function api(url, options = {}, auth = false) {
  const headers = options.headers || {};
  if (auth) {
    const token = getToken();
    if (!token) { alert('로그인이 필요합니다.'); location.href = '/login.html'; throw new Error('로그인 필요'); }
    headers['Authorization'] = 'Bearer ' + token;
  }
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || '요청 실패');
  }
  const contentType = response.headers.get('content-type') || '';
  return contentType.includes('application/json') ? response.json() : response.text();
}
