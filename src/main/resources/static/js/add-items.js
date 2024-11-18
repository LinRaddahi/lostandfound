document.getElementById('uploadButton').addEventListener('click', async function () {
    const fileInput = document.getElementById('pdfUpload');
    const file = fileInput.files[0];

    if (file && file.type === 'application/pdf') {
        const uploadButton = document.getElementById('uploadButton');
        uploadButton.disabled = true;

        const formData = new FormData();
        formData.append('lostItemsFile', file);

        try {
            const response = await fetch('/lost-items', {
                method: 'POST',
                headers: {'Authorization': 'Bearer ' + localStorage.getItem('jwtToken')},
                body: formData
            });

            if (response.ok) {
                alert('File uploaded successfully');
            } else {
                alert('Failed to upload file');
            }
        } catch (error) {
            alert('An error occurred while uploading the file');
        } finally {
            uploadButton.disabled = false;
        }
    } else {
        alert('Please select a valid PDF file');
    }
});
