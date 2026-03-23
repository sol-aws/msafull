const API_BASE = '';
const PLACEHOLDER_IMAGE = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80';

function saveAuth(data) {
  localStorage.setItem('auth', JSON.stringify(data));
}

function getAuth() {
  const raw = localStorage.getItem('auth');
  return raw ? JSON.parse(raw) : null;
}

function clearAuth() {
  localStorage.removeItem('auth');
}

function getToken() {
  const auth = getAuth();
  return auth ? auth.token : null;
}

function toggleAuthMenus(isLoggedIn) {
  document.querySelectorAll('.auth-guest').forEach(function (element) {
    element.style.display = isLoggedIn ? 'none' : '';
  });
  document.querySelectorAll('.auth-user').forEach(function (element) {
    element.style.display = isLoggedIn ? '' : 'none';
  });
}

function renderHeader() {
  const auth = getAuth();
  const userBox = document.getElementById('userBox');
  toggleAuthMenus(!!auth);

  if (!userBox) {
    return;
  }

  if (auth) {
    userBox.innerHTML = `
      <div class="user-pill">
        <span class="user-name">${auth.nickname || auth.name || auth.loginId || auth.email}님</span>
        <button class="btn btn-danger btn-sm" id="logoutBtn" type="button">로그아웃</button>
      </div>
    `;
    const logoutBtn = document.getElementById('logoutBtn');
    logoutBtn.addEventListener('click', function () {
      clearAuth();
      window.location.href = '/';
    });
  } else {
    userBox.innerHTML = '';
  }
}

function showMessage(targetId, message, type) {
  const target = document.getElementById(targetId);
  if (!target) {
    return;
  }
  target.className = `message ${type}`;
  target.textContent = message;
  target.style.display = 'block';
}

function extractErrorMessage(text) {
  if (!text) {
    return '요청 처리 중 오류가 발생했습니다.';
  }

  if (text.includes('504 Gateway Time-out') || text.includes('504 Gateway Timeout')) {
    return '구매 요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.';
  }

  if (text.includes('<html') || text.includes('<!DOCTYPE') || text.includes('<body')) {
    return '서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
  }

  return text;
}

async function apiRequest(url, options = {}) {
  const headers = options.headers || {};
  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(extractErrorMessage(text));
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }
  return response.text();
}

function getImageUrl(url) {
  return url || PLACEHOLDER_IMAGE;
}

function formatPrice(value) {
  return `${Number(value || 0).toLocaleString()}원`;
}

function getProductDetailUrl(productId) {
  return `/product-detail.html?id=${productId}`;
}

function goToProductDetail(productId) {
  window.location.href = getProductDetailUrl(productId);
}

window.goToProductDetail = goToProductDetail;

function getQueryParam(name) {
  const params = new URLSearchParams(window.location.search);
  return params.get(name);
}

async function loadProducts() {
  const productList = document.getElementById('productList');
  if (!productList) {
    return;
  }

  try {
    const products = await apiRequest('/product-service/product/list', { method: 'GET', headers: {} });
    if (!products.length) {
      productList.innerHTML = '<div class="empty">등록된 상품이 없습니다. 첫 상품을 등록해보세요.</div>';
      return;
    }

    productList.innerHTML = products.map(function (product) {
      return `
        <article class="product-card" onclick="goToProductDetail(${product.id})" role="button" tabindex="0">
          <div class="product-image-wrap">
            <img src="${getImageUrl(product.imageUrl)}" alt="${product.name}">
          </div>
          <div class="product-body">
            <div class="product-brand">${product.category || '추천 상품'}</div>
            <div class="product-title">${product.name}</div>
            <div class="product-price">${formatPrice(product.price)}</div>
            <div class="product-stock">재고 ${product.stockQuantity}개</div>
            <div class="product-description">${product.description || '상품 상세 페이지에서 자세한 설명을 확인하세요.'}</div>
          </div>
        </article>
      `;
    }).join('');
  } catch (error) {
    productList.innerHTML = '<div class="empty">상품 목록을 불러오지 못했습니다.</div>';
  }
}

function guardLogin(redirectPath) {
  if (!getToken()) {
    alert('로그인 후 이용해주세요.');
    window.location.href = redirectPath || '/login.html';
    return false;
  }
  return true;
}

async function loadProductDetail() {
  const target = document.getElementById('productDetail');
  if (!target) {
    return;
  }

  const productId = getQueryParam('id');
  if (!productId) {
    target.innerHTML = '<div class="empty">상품 정보가 없습니다.</div>';
    return;
  }

  try {
    const product = await apiRequest(`/product-service/product/${productId}`, { method: 'GET', headers: {} });
    target.innerHTML = `
      <section class="detail-card">
        <div class="detail-image-wrap">
          <img class="detail-image" src="${getImageUrl(product.imageUrl)}" alt="${product.name}">
        </div>
        <div class="detail-content">
          <div class="detail-category">${product.category || '카테고리 없음'}</div>
          <h1 class="detail-title">${product.name}</h1>
          <div class="detail-price">${formatPrice(product.price)}</div>
          <div class="detail-stock" id="detailStock">재고: ${product.stockQuantity}개</div>
          <p class="detail-description">${product.description || '상품 설명이 없습니다.'}</p>
          <div class="detail-actions">
            <button class="btn btn-primary" id="purchaseBtn" type="button" ${product.stockQuantity < 1 ? 'disabled' : ''}>구매하기</button>
            <a class="btn btn-secondary" href="/">메인으로</a>
          </div>
          <div id="purchaseMessage" style="display:none;"></div>
        </div>
      </section>
    `;

    const purchaseBtn = document.getElementById('purchaseBtn');
    if (purchaseBtn) {
      purchaseBtn.addEventListener('click', async function () {
        if (!guardLogin('/login.html')) {
          return;
        }

        if (product.stockQuantity < 1) {
          showMessage('purchaseMessage', '재고가 부족합니다.', 'error');
          return;
        }

        const originalText = purchaseBtn.textContent;
        purchaseBtn.disabled = true;
        purchaseBtn.textContent = '구매 처리중...';
        try {
          await apiRequest(`/product-service/product/${product.id}/purchase`, {
            method: 'POST'
          });
          alert('구매되었습니다.');
          window.location.href = '/';
        } catch (error) {
          purchaseBtn.disabled = false;
          purchaseBtn.textContent = originalText;
          showMessage('purchaseMessage', error.message || '구매 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.', 'error');
        }
      });
    }
  } catch (error) {
    target.innerHTML = '<div class="empty">상품 상세 정보를 불러오지 못했습니다.</div>';
  }
}
