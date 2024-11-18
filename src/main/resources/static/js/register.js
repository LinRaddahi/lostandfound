document.getElementById('registerForm').addEventListener('submit', async function (event) {

    event.preventDefault();

    const email = document.getElementById('email').value;
    const name = document.getElementById('name').value;
    const password = document.getElementById('password').value;

    const response = await fetch('/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            'email': email,
            'name': name,
            'password': password
        })
    });

    if (response.ok) {
        console.log("Registered successfully");
        const data = await response;
        const token = data.headers.get('Authorization').substring(7);

        // Store the token in localStorage
        localStorage.setItem('jwtToken', token);

        window.location.href = '/home.html';
    } else {
        alert('Login failed');
    }
});