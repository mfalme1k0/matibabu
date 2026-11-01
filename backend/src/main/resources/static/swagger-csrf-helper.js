/**
 * Matibabu Swagger UI helper:
 * - Seeds the XSRF-TOKEN cookie via GET /csrf
 * - Ensures Try-it-out requests send session cookies (credentials: include)
 * - Attaches X-XSRF-TOKEN from the cookie on mutating requests
 *
 * Login first (same browser origin): POST /login with form fields email + password,
 * or use the app login page. Session cookie JSESSIONID is httpOnly; CSRF cookie is not.
 */
(function () {
  function readCookie(name) {
    const match = document.cookie.match(new RegExp('(?:^|; )' + name.replace(/([.$?*|{}()[\]\\/+^])/g, '\\$1') + '=([^;]*)'));
    return match ? decodeURIComponent(match[1]) : null;
  }

  function seedCsrf() {
    return fetch('/csrf', { method: 'GET', credentials: 'include' })
      .then(function (r) { return r.ok ? r.json().catch(function () { return null; }) : null; })
      .catch(function () { return null; });
  }

  function installBanner() {
    if (document.getElementById('matibabu-swagger-auth-banner')) return;
    const banner = document.createElement('div');
    banner.id = 'matibabu-swagger-auth-banner';
    banner.setAttribute('role', 'note');
    banner.style.cssText = [
      'position:sticky', 'top:0', 'z-index:10000',
      'background:#1b3a4b', 'color:#f5f7fa',
      'padding:10px 16px', 'font:14px/1.4 system-ui,sans-serif',
      'border-bottom:3px solid #3d8bfd'
    ].join(';');
    banner.innerHTML = [
      '<strong>Auth for Try it out</strong> — ',
      '1) Open <code style="color:#9cdcfe">/csrf</code> once (cookie seeded automatically). ',
      '2) Login at <code style="color:#9cdcfe">/login</code> (same browser) — local seed admin: <code>admin@example.com</code> / <code>Password</code>. ',
      '3) Mutating calls send <code>JSESSIONID</code> + <code>X-XSRF-TOKEN</code> automatically.'
    ].join('');
    document.body.insertBefore(banner, document.body.firstChild);
  }

  function patchUi(ui) {
    if (!ui || typeof ui.getConfigs !== 'function') return;
    const configs = ui.getConfigs();
    const previous = configs.requestInterceptor;
    configs.requestInterceptor = function (req) {
      req.credentials = 'include';
      const token = readCookie('XSRF-TOKEN');
      if (token) {
        req.headers = req.headers || {};
        req.headers['X-XSRF-TOKEN'] = token;
      }
      return typeof previous === 'function' ? previous(req) : req;
    };
  }

  function tryPatch() {
    if (window.ui) {
      patchUi(window.ui);
      return true;
    }
    return false;
  }

  function boot() {
    installBanner();
    seedCsrf().then(function () {
      if (tryPatch()) return;
      let attempts = 0;
      const timer = setInterval(function () {
        attempts += 1;
        if (tryPatch() || attempts > 40) clearInterval(timer);
      }, 250);
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
