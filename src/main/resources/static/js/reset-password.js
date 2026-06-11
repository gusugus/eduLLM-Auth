document.getElementById('resetForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const token = document.getElementById('token').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        document.getElementById('message').textContent = "Las contraseñas no coinciden.";
        document.getElementById('message').style.color = "red";
        return;
    }

    try {
        const response = await fetch('/api/auth/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token: token, newPassword: newPassword })
        });
        const data = await response.text();
        document.getElementById('message').textContent = data;
        document.getElementById('message').style.color = response.ok ? "green" : "red";
        if (response.ok) {
            setTimeout(() => window.location.href = '/login', 2000);
        }
    } catch (error) {
        document.getElementById('message').textContent = "Error de conexión";
        document.getElementById('message').style.color = "red";
    }
});
