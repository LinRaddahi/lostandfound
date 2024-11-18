document.addEventListener('DOMContentLoaded', async function () {

    const token = localStorage.getItem('jwtToken');

    const response = await fetch('/lost-items/claims', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + token
        }
    });

    if (response.ok) {
        const page = await response.json();
        if (page) {
            displayClaimedItems(page.content);
        } else {
            console.error("Expected a Page object with a content array but got:", page);
            alert('Failed to load items');
        }
    } else {
        console.log("Fetch failed with status:", response.status);
        alert('Fetch failed');
    }

    function displayClaimedItems(items) {
        const itemsContainer = document.getElementById('itemsContainer');
        itemsContainer.innerHTML = ''; // Clear any existing content

        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.className = 'item';
            itemElement.innerHTML = `
                <h3>Claim ${item.id}</h3>
                <p><strong>Item:</strong> ${item.lostItem.itemName}</p>
                <p><strong>Place:</strong> ${item.lostItem.place}</p>
                <p><strong>Quantity:</strong> ${item.quantity}</p>
                <p><strong>Claimed by User ID:</strong> ${item.user.id}</p>
            `;
            itemsContainer.appendChild(itemElement);
        });

    }

});