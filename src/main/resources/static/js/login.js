const loginForm = document.getElementById('loginForm');
const messageDiv = document.getElementById('message');

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    messageDiv.textContent = '';
    messageDiv.classList.remove('error', 'success');

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            messageDiv.textContent = 'Login exitoso. Redirigiendo...';
            messageDiv.classList.add('success');

            setTimeout(() => {
                window.location.href = data.redirectUrl;
            }, 5500);
        } else {
            const errorMsg = data.error || data.message || 'Credenciales inválidas';
            messageDiv.textContent = errorMsg;
            messageDiv.classList.add('error');
        }
    } catch (error) {
        console.error('Fetch error:', error);
        messageDiv.textContent = 'Error de conexión con el servidor';
        messageDiv.classList.add('error');
    }
});
