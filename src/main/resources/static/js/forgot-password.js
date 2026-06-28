const forgotForm = document.getElementById('forgotForm');
const messageDiv = document.getElementById('message');
const submitBtn = forgotForm.querySelector('button[type="submit"]');
const originalBtnHtml = submitBtn.innerHTML;

const spinnerSvg = '<svg class="inline animate-spin h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>';

function setLoading(loading) {
    submitBtn.disabled = loading;
    submitBtn.innerHTML = loading ? spinnerSvg + 'Enviando...' : originalBtnHtml;
    submitBtn.classList.toggle('opacity-50', loading);
    submitBtn.classList.toggle('cursor-not-allowed', loading);
}

forgotForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;

    setLoading(true);

    try {
        const response = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: username })
        });
        const data = await response.text();
        messageDiv.textContent = data;
    } catch (error) {
        messageDiv.textContent = "Error de conexión";
    } finally {
        setLoading(false);
    }
});
