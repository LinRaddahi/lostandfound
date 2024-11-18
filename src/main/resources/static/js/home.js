document.addEventListener('DOMContentLoaded', async function () {

    // Decode the token to get the claims
    const response = await fetch('/get-user', {
        method: 'GET',
        headers: {'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')}
    });

    if (response.ok) {
        const user = await response.json();
    if (user.roles.some(role => role.name  === 'ROLE_ADMIN')) {
        document.getElementById('adminButtons').style.display = 'block';
    } else {
        document.getElementById('adminButtons').style.display = 'none';
    }}
    else {
        console.error('Failed to get user:', response.status);
    }
});

document.addEventListener('click', async function (event) {

    if (event.target.id ==='all-items') {
        window.location.href = '/lost-items.html';
    }

    if (event.target.id ==='claimed-items') {
        window.location.href = '/claimed-items.html';
    }

    if (event.target.id ==='add-items') {
        window.location.href = '/add-items.html';
    }

    if (event.target.id ==='logout') {
        localStorage.removeItem('jwtToken');
        window.location.href = '/login.html';
    }
});
