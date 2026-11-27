/* Shared helpers used by every page */
const App = {
  async api(path, opts = {}) {
    const init = { credentials: 'same-origin', ...opts, headers: { ...(opts.headers || {}) } };
    if (opts.json !== undefined) {
      init.method = opts.method || 'POST';
      init.headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(opts.json);
    }
    const res = await fetch(path, init);
    if (res.status === 401 && !path.startsWith('/api/auth/')) { location.href = '/index.html'; throw new Error('Session expired'); }
    const text = await res.text();
    let data = null; try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }
    if (!res.ok) throw new Error((data && data.message) || ('Request failed (' + res.status + ')'));
    return data;
  },
  toast(msg, type = '') {
    let t = document.getElementById('toast');
    if (!t) { t = document.createElement('div'); t.id = 'toast'; t.className = 'toast'; document.body.appendChild(t); }
    t.textContent = msg; t.className = 'toast show ' + type;
    clearTimeout(App._tt); App._tt = setTimeout(() => t.className = 'toast', 3500);
  },
  esc(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); },
  fmt(d) { return d ? new Date(d).toLocaleString() : ''; },
  label(v) { return String(v ?? '').replace(/_/g, ' ').replace(/\w\S*/g, w => w[0] + w.slice(1).toLowerCase()); },
  badge(s) { return `<span class="badge b-${s}">${App.label(s)}</span>`; },
  pbadge(p) { return `<span class="badge p-${p}">${App.label(p)}</span>`; },
  CATEGORIES: ['POTHOLE', 'GARBAGE', 'WATER_LEAKAGE', 'STREETLIGHT', 'DRAINAGE', 'BROKEN_ROAD', 'OTHER'],
  STATUSES: ['PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'],
  PRIORITIES: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
  options(list, blank) { return (blank ? `<option value="">${blank}</option>` : '') + list.map(v => `<option value="${v}">${App.label(v)}</option>`).join(''); },
  async requireRole(role) {
    try {
      const me = await App.api('/api/auth/me');
      if (me.role !== role) { location.href = '/index.html'; return null; }
      const w = document.getElementById('whoami'); if (w) w.textContent = me.fullName + ' (' + App.label(me.role) + ')';
      return me;
    } catch (e) { location.href = '/index.html'; return null; }
  },
  async logout() { try { await App.api('/api/auth/logout', { method: 'POST' }); } catch (e) {} location.href = '/index.html'; },
  tabs(defaultTab, onShow) {
    const btns = document.querySelectorAll('nav.tabs button');
    const show = id => {
      btns.forEach(b => b.classList.toggle('active', b.dataset.tab === id));
      document.querySelectorAll('section.tab').forEach(s => s.classList.toggle('hidden', s.id !== id));
      if (onShow) onShow(id);
    };
    btns.forEach(b => b.onclick = () => show(b.dataset.tab));
    show(defaultTab);
  },
  openModal(html) { const m = document.getElementById('modal'); m.querySelector('.modal').innerHTML = html; m.classList.add('show'); },
  closeModal() { document.getElementById('modal').classList.remove('show'); },
  /* Renders the complaint detail (photo + history) used by all roles */
  detailHtml(c) {
    const hist = (c.history || []).map(h => `<li>${App.badge(h.status)} <small>${App.fmt(h.createdAt)}${h.updatedBy ? ' · ' + App.esc(h.updatedBy) : ''}</small><br>${App.esc(h.remark || '')}</li>`).join('');
    return `<h2>${App.esc(c.complaintCode)} – ${App.esc(c.title)}</h2>
      <p>${App.badge(c.status)} ${App.pbadge(c.priority)} <span class="muted">${App.label(c.category)}</span></p>
      <p>${App.esc(c.description)}</p>
      <p><b>Location:</b> ${App.esc(c.address)}${c.latitude != null ? ` <span class="muted">(${c.latitude.toFixed(5)}, ${c.longitude.toFixed(5)})</span>` : ''}</p>
      <p><b>Reported by:</b> ${App.esc(c.citizenName)} · ${App.fmt(c.createdAt)}<br>
         <b>Department:</b> ${App.esc(c.departmentName || '—')} · <b>Officer:</b> ${App.esc(c.officerName || '—')}</p>
      ${c.imageUrl ? `<img class="photo" src="${c.imageUrl}" alt="Complaint photo">` : ''}
      <h3>History</h3><ul class="timeline">${hist}</ul>`;
  }
};
