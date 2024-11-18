document.addEventListener('DOMContentLoaded', async function () {

    const token = localStorage.getItem('jwtToken');

    const response = await fetch('/lost-items', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    });

    if (response.ok) {
        const page = await response.json();
        if (page) {
            displayLostItems(page.content);
        } else {
            console.error("Expected a Page object with a content array but got:", page);
            alert('Failed to load items');
        }
    } else {
        console.log("Fetch failed with status:", response.status);
        alert('Fetch failed');
    }

    function displayLostItems(items) {
        const itemsContainer = document.getElementById('itemsContainer');
        itemsContainer.innerHTML = ''; // Clear any existing content

        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.className = 'item';
            itemElement.innerHTML = `
                <h3>${item.itemName}</h3>
                <p><strong>Quantity:</strong> ${item.quantity}</p>
                <p><strong>Place:</strong> ${item.place}</p>
                <button class="claim-button" data-id="${item.id}">Claim</button>

            `;
            itemsContainer.appendChild(itemElement);
        });

        document.querySelectorAll('.claim-button').forEach(button => {
            button.addEventListener('click', function () {
                document.getElementById('itemId').value = this.getAttribute('data-id');
                document.getElementById('claimModal').style.display = 'block';
            });
        });
    }

    // Modal handling
    const modal = document.getElementById('claimModal');
    const span = document.getElementsByClassName('close')[0];

    span.onclick = function () {
        modal.style.display = 'none';
    }

    window.onclick = function (event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    }

    document.getElementById('claimForm').addEventListener('submit', async function (event) {
        event.preventDefault();

        const itemId = document.getElementById('itemId').value;
        const quantity = document.getElementById('claimQuantity').value;

        const response = await fetch(`/lost-items/${itemId}/claim?quantity=${quantity}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')
            }
        });

        if (response.ok) {
            alert('Item claimed successfully');
            modal.style.display = 'none';
        } else {
            alert('Failed to claim item');
        }
    });
});