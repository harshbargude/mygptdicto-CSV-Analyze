// Outlier method change handler
document.getElementById('outlier_method').addEventListener('change', function () {
    const thresholdDescription = document.getElementById('threshold_description');
    if (this.value === 'iqr') {
        thresholdDescription.textContent = 'values outside Q1-1.5*IQR and Q3+1.5*IQR';
    } else if (this.value === 'zscore') {
        thresholdDescription.textContent = 'values with z-score greater than threshold';
    }
});

// Outlier threshold slider
document.getElementById('outlier_threshold').addEventListener('input', function () {
    document.getElementById('outlier_threshold_value').textContent = this.value;
});

// Preview cleaning button handler
document.getElementById('preview-cleaning-btn').addEventListener('click', function () {
    // Show the modal
    document.getElementById('preview-modal').classList.remove('hidden');

    // Get form data
    const formData = new FormData(document.getElementById('cleaning-form'));

    // Send AJAX request to get preview
    fetch('/preview-cleaning', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            // Populate before cleaning table
            const beforeBody = document.getElementById('before-cleaning-body');
            beforeBody.innerHTML = '';

            data.beforeCleaning.forEach(row => {
                const tr = document.createElement('tr');
                Object.values(row).forEach(value => {
                    const td = document.createElement('td');
                    td.className = 'border px-3 py-2 text-xs';
                    td.textContent = value !== null ? value : 'null';
                    tr.appendChild(td);
                });
                beforeBody.appendChild(tr);
            });

            // Populate after cleaning header
            const afterHeader = document.getElementById('after-cleaning-header');
            afterHeader.innerHTML = '';
            const headerRow = document.createElement('tr');

            data.cleanedColumns.forEach(col => {
                const th = document.createElement('th');
                th.className = 'px-3 py-2 bg-gray-100 text-left text-gray-600 text-xs';
                th.textContent = col;
                headerRow.appendChild(th);
            });
            afterHeader.appendChild(headerRow);

            // Populate after cleaning table
            const afterBody = document.getElementById('after-cleaning-body');
            afterBody.innerHTML = '';

            data.afterCleaning.forEach(row => {
                const tr = document.createElement('tr');
                data.cleanedColumns.forEach(col => {
                    const td = document.createElement('td');
                    td.className = 'border px-3 py-2 text-xs';
                    td.textContent = row[col] !== null ? row[col] : 'null';
                    tr.appendChild(td);
                });
                afterBody.appendChild(tr);
            });

            // Populate cleaning summary
            const cleaningSummary = document.getElementById('cleaning-summary');
            cleaningSummary.innerHTML = `
<h4 class="font-medium mb-2">Summary of Changes:</h4>
<ul class="list-disc ml-5 space-y-1">
${data.stats.rowsRemoved > 0 ? `<li>${data.stats.rowsRemoved} rows would be removed</li>` : ''}
${data.stats.columnsAdded > 0 ? `<li>${data.stats.columnsAdded} columns would be added</li>` : ''}
${data.stats.columnsRemoved > 0 ? `<li>${data.stats.columnsRemoved} columns would be removed</li>` : ''}
${data.stats.nullValuesFilled > 0 ? `<li>${data.stats.nullValuesFilled} missing values would be filled</li>` : ''}
${data.stats.duplicatesRemoved > 0 ? `<li>${data.stats.duplicatesRemoved} duplicate rows would be removed</li>` : ''}
${data.stats.outliersRemoved > 0 ? `<li>${data.stats.outliersRemoved} outliers would be removed</li>` : ''}
</ul>
`;
        })
        .catch(error => {
            console.error('Error fetching preview:', error);
        });
});

// Close preview modal
document.getElementById('close-preview').addEventListener('click', function () {
    document.getElementById('preview-modal').classList.add('hidden');
});

// Close modal when clicking outside
document.getElementById('preview-modal').addEventListener('click', function (event) {
    if (event.target === this) {
        this.classList.add('hidden');
    }
});

// File upload display
document.getElementById('file-upload').addEventListener('change', function (e) {
    const fileName = e.target.files[0].name;
    const fileLabel = this.previousElementSibling;
    const fileLabelText = fileLabel.querySelector('p.mb-2');
    fileLabelText.textContent = fileName;
});

// Initialize the page
document.addEventListener('DOMContentLoaded', function () {
    // Set initial values for sliders
    document.getElementById('null_threshold_value').textContent =
        document.getElementById('null_threshold').value;

    document.getElementById('outlier_threshold_value').textContent =
        document.getElementById('outlier_threshold').value;
});



//// to add th sho detils button -- script
document.getElementById('show-columns-btn').addEventListener('click', function() {
    const infoPanel = document.getElementById('columns-info');
    const icon = this.querySelector('svg');

    console.log("show info button!")
    
    if (infoPanel.classList.contains('hidden')) {
        infoPanel.classList.remove('hidden');
        icon.classList.add('rotate-180');
    } else {
        infoPanel.classList.add('hidden');
        icon.classList.remove('rotate-180');
    }
});