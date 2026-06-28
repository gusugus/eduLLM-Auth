document.addEventListener('DOMContentLoaded', async function() {
    const token = document.getElementById('token').value;
    const form = document.getElementById('resetForm');
    const messageDiv = document.getElementById('message');
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalBtnHtml = submitBtn.innerHTML;

    const spinnerSvg = '<svg class="inline animate-spin h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>';

    function setLoading(loading) {
        submitBtn.disabled = loading;
        submitBtn.innerHTML = loading ? spinnerSvg + 'Restableciendo...' : originalBtnHtml;
        submitBtn.classList.toggle('opacity-50', loading);
        submitBtn.classList.toggle('cursor-not-allowed', loading);
    }

    try {
        const verifyRes = await fetch('/api/auth/verify-reset-token?token=' + encodeURIComponent(token));
        const verifyData = await verifyRes.json();
        if (!verifyData.valid) {
            messageDiv.textContent = verifyData.message || "El enlace ha expirado.";
            messageDiv.style.color = "red";
            form.style.display = "none";
            return;
        }
    } catch (error) {
        messageDiv.textContent = "Error al verificar el enlace.";
        messageDiv.style.color = "red";
        form.style.display = "none";
        return;
    }

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (newPassword !== confirmPassword) {
            messageDiv.textContent = "Las contraseñas no coinciden.";
            messageDiv.style.color = "red";
            return;
        }

        setLoading(true);

        try {
            const response = await fetch('/api/auth/reset-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token: token, newPassword: newPassword })
            });
            const data = await response.text();
            messageDiv.textContent = data;
            messageDiv.style.color = response.ok ? "green" : "red";
            if (response.ok) {
                setTimeout(() => window.location.href = '/login', 2000);
            } else {
                setLoading(false);
            }
        } catch (error) {
            messageDiv.textContent = "Error de conexión";
            messageDiv.style.color = "red";
            setLoading(false);
        }
    });
});
