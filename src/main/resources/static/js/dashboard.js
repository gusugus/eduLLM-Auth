function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Error decoding token:', e);
        return null;
    }
}

const token = localStorage.getItem('jwtToken');
if (!token) {
    window.location.href = '/login';
}

document.getElementById('tokenDisplay').innerText = token;

const payload = parseJwt(token);
if (payload) {
    document.getElementById('payloadDisplay').innerText = JSON.stringify(payload, null, 2);
    document.getElementById('idUsuario').innerText = payload.idUsuario || payload.idUsuario !== undefined ? payload.idUsuario : 'no presente';
    document.getElementById('rol').innerText = payload.rol || 'no presente';
    document.getElementById('username').innerText = payload.sub || 'no presente';
} else {
    document.getElementById('payloadDisplay').innerText = 'No se pudo decodificar el token';
}

document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.removeItem('jwtToken');
    window.location.href = '/login';
});
