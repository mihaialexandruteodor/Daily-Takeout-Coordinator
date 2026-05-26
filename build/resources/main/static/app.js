function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

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
    patchParticipants(data);
}

function showError(body) {
    alert(body.error || 'An unexpected error occurred.');
}

async function setStatus(status) {
    const res = await fetch('/api/status', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({status})
    });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

async function addProposal() {
    const input = document.getElementById('restaurant-input');
    const restaurant = input.value.trim();
    if (!restaurant) return;

    const res = await fetch('/api/proposals', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({restaurant})
    });
    if (!res.ok) { showError(await res.json()); return; }
    input.value = '';
    patchView(await res.json());
}

async function castVote(proposalId) {
    const res = await fetch('/api/votes', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({proposalId})
    });
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

async function retractVote(proposalId) {
    const res = await fetch(`/api/votes/by-proposal/${proposalId}`, {method: 'DELETE'});
    if (!res.ok) { showError(await res.json()); return; }
    patchView(await res.json());
}

// Poll every 10 seconds to sync with other users
setInterval(async () => {
    const res = await fetch('/api/view');
    if (res.ok) patchView(await res.json());
}, 10000);
