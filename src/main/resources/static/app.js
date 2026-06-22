// ── Utilities ─────────────────────────────────────────────────────────────────

function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function showError(body) {
    alert(body.error || 'An unexpected error occurred.');
}

// ── Name modal ────────────────────────────────────────────────────────────────

function openNameModal() {
    document.getElementById('name-modal-overlay').style.display = 'flex';
    const input = document.getElementById('modal-name-input');
    input.value = '';
    document.getElementById('modal-name-error').textContent = '';
    input.focus();
}

async function submitModalName() {
    const input = document.getElementById('modal-name-input');
    const errorEl = document.getElementById('modal-name-error');
    const name = input.value.trim();
    if (!name) {
        errorEl.textContent = 'Please enter your name.';
        input.focus();
        return;
    }
    errorEl.textContent = '';

    const res = await fetch('/api/join-inline', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
    });

    if (res.status === 409) {
        const body = await res.json();
        errorEl.textContent = body.error || 'Name already taken.';
        input.select();
        return;
    }
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        errorEl.textContent = body.error || 'Something went wrong, please try again.';
        return;
    }

    // Success: hide modal + join button, update nav, render view
    document.getElementById('name-modal-overlay').style.display = 'none';
    const joinBtn = document.getElementById('btn-join-modal');
    if (joinBtn) joinBtn.style.display = 'none';
    const data = await res.json();
    const navUsername = document.getElementById('nav-username');
    if (navUsername) navUsername.textContent = '👋 ' + data.currentUser;
    patchView(data);
}

// ── DOM patching ──────────────────────────────────────────────────────────────

function patchStatus(data) {
    const hasFoodBtn = document.getElementById('btn-has-food');
    const orderingBtn = document.getElementById('btn-ordering');
    if (!hasFoodBtn || !orderingBtn) return;
    hasFoodBtn.className = 'button is-medium' + (data.currentUserStatus === 'HAS_FOOD' ? ' is-success' : '');
    orderingBtn.className = 'button is-medium' + (data.currentUserStatus === 'ORDERING' ? ' is-warning' : '');
}

function patchProposals(data) {
    const list = document.getElementById('proposals-list');
    const inputRow = document.getElementById('proposal-input-row');
    if (!list) return;

    if (data.proposals.length === 0) {
        list.innerHTML = '<div class="has-text-grey has-text-centered py-4">No proposals yet. Be the first to suggest a restaurant!</div>';
    } else {
        list.innerHTML = data.proposals.map(p => `
            <div class="proposal-row level is-mobile mb-3">
                <div class="level-left">
                    <div>
                        <span class="has-text-weight-semibold">${escHtml(p.restaurant)}</span>
                        <span class="tag is-info is-light ml-2">${p.voteCount} ${p.voteCount === 1 ? 'vote' : 'votes'}</span>
                    </div>
                </div>
                <div class="level-right">
                    ${p.votedByCurrentUser
                        ? `<button class="button is-small is-danger is-outlined" onclick="retractVote(${p.id})">✕ Unvote</button>`
                        : `<button class="button is-small is-primary is-outlined" onclick="castVote(${p.id})">👍 Vote</button>`
                    }
                </div>
            </div>
        `).join('');
    }

    if (inputRow) {
        inputRow.style.display = data.currentUserStatus === 'ORDERING' ? '' : 'none';
    }
}

function patchTimeProposals(data) {
    const list = document.getElementById('time-proposals-list');
    if (!list) return;

    const items = data.timeProposals || [];
    if (items.length === 0) {
        list.innerHTML = '<div class="has-text-grey has-text-centered py-4">No times proposed yet. Suggest when to go pick up!</div>';
    } else {
        list.innerHTML = items.map(tp => `
            <div class="proposal-row level is-mobile mb-3">
                <div class="level-left">
                    <div>
                        <span class="has-text-weight-semibold">${escHtml(tp.proposedTime)}</span>
                        <span class="has-text-grey-light is-size-7 ml-2">by ${escHtml(tp.proposedBy)}</span>
                        <span class="tag is-warning is-light ml-2">${tp.voteCount} ${tp.voteCount === 1 ? 'vote' : 'votes'}</span>
                    </div>
                </div>
                <div class="level-right">
                    ${tp.votedByCurrentUser
                        ? `<button class="button is-small is-danger is-outlined" onclick="retractTimeVote(${tp.id})">✕ Unvote</button>`
                        : `<button class="button is-small is-warning is-outlined" onclick="castTimeVote(${tp.id})">👍 Vote</button>`
                    }
                </div>
            </div>
        `).join('');
    }
}

function patchParticipants(data) {
    const list = document.getElementById('participants-list');
    if (!list) return;
    list.innerHTML = data.participants.map(p => {
        const statusClass = p.status === 'HAS_FOOD'
            ? 'is-success is-light'
            : p.status === 'ORDERING'
                ? 'is-warning is-light'
                : 'is-light';
        const statusLabel = p.status ? p.status.replace('_', ' ') : 'Undecided';
        return `
            <div class="participant-row level is-mobile mb-2">
                <div class="level-left"><span>${escHtml(p.userName)}</span></div>
                <div class="level-right"><span class="tag ${statusClass}">${escHtml(statusLabel)}</span></div>
            </div>
        `;
    }).join('');
}

function patchView(data) {
    patchStatus(data);
    patchProposals(data);
    patchTimeProposals(data);
    patchParticipants(data);
}

// ── Status ────────────────────────────────────────────────────────────────────

async function setStatus(status) {
    const res = await fetch('/api/status', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
    });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

// ── Restaurant proposals ──────────────────────────────────────────────────────

async function addProposal() {
    const input = document.getElementById('restaurant-input');
    const restaurant = input.value.trim();
    if (!restaurant) return;
    const res = await fetch('/api/proposals', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ restaurant })
    });
    if (!res.ok) { showError(await res.json()); return; }
    input.value = '';
    patchView(await res.json());
}

async function castVote(proposalId) {
    const res = await fetch('/api/votes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ proposalId })
    });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

async function retractVote(proposalId) {
    const res = await fetch(`/api/votes/by-proposal/${proposalId}`, { method: 'DELETE' });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

// ── Time proposals ────────────────────────────────────────────────────────────

async function addTimeProposal() {
    const input = document.getElementById('time-input');
    const proposedTime = input.value.trim();
    if (!proposedTime) return;
    const res = await fetch('/api/time-proposals', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ proposedTime })
    });
    if (!res.ok) { showError(await res.json()); return; }
    input.value = '';
    patchView(await res.json());
}

async function castTimeVote(timeProposalId) {
    const res = await fetch('/api/time-votes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ timeProposalId })
    });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

async function retractTimeVote(timeProposalId) {
    const res = await fetch(`/api/time-votes/by-proposal/${timeProposalId}`, { method: 'DELETE' });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

// ── Polling ───────────────────────────────────────────────────────────────────

setInterval(async () => {
    const res = await fetch('/api/view');
    if (res.ok) patchView(await res.json());
}, 10000);
