const loginForm = document.getElementById('loginForm');
const messageDiv = document.getElementById('message');
const submitBtn = loginForm.querySelector('button[type="submit"]');
const originalBtnHtml = submitBtn.innerHTML;

const spinnerSvg = '<svg class="inline animate-spin h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>';

function setLoading(loading) {
    submitBtn.disabled = loading;
    submitBtn.innerHTML = loading ? spinnerSvg + 'Ingresando...' : originalBtnHtml;
    submitBtn.classList.toggle('opacity-50', loading);
    submitBtn.classList.toggle('cursor-not-allowed', loading);
}

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    messageDiv.textContent = '';
    messageDiv.classList.remove('error', 'success');

    setLoading(true);

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            const delay = data.redirectDelay || 2500;

            if (data.mustChangePassword) {
                messageDiv.textContent = 'Debes cambiar tu contraseña. Redirigiendo...';
                messageDiv.classList.add('success');
                setTimeout(() => {
                    window.location.href = '/reset-password?token=' + encodeURIComponent(data.resetToken);
                }, delay);
                return;
            }

            messageDiv.textContent = 'Login exitoso. Redirigiendo...';
            messageDiv.classList.add('success');

            setTimeout(() => {
                window.location.href = data.redirectUrl;
            }, delay);
        } else {
            const errorMsg = data.error || data.message || 'Credenciales inválidas';
            messageDiv.textContent = errorMsg;
            messageDiv.classList.add('error');
            setLoading(false);
        }
    } catch (error) {
        console.error('Fetch error:', error);
        messageDiv.textContent = 'Error de conexión con el servidor';
        messageDiv.classList.add('error');
        setLoading(false);
    }
});
