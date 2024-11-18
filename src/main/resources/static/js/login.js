document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const response = await fetch(`/authenticate?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });

    if (response.ok) {
        console.log("Login successful");
        const token = response.headers.get('Authorization').substring(7);

        // Store the token in localStorage
        localStorage.setItem('jwtToken', token);

        window.location.href = '/home.html';
    } else {
        alert(`Login failed: ${response.status} ${await response.text()}`);
    }
});

document.getElementById('registerButton').addEventListener('click', async function () {
    window.location.href = '/register.html';
});

