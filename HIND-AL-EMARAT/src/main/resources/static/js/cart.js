/* ==========================================
   HIND AL EMARAT - Cart & Checkout Helpers
   ========================================== */

document.addEventListener('DOMContentLoaded', function() {
    // 1. Quantity Control Selector on Detail Page
    const qtyInput = document.querySelector('.quantity-val');
    const btnMinus = document.querySelector('.qty-minus');
    const btnPlus = document.querySelector('.qty-plus');
    const hiddenQtyInput = document.getElementById('hidden-qty-input');

    if (qtyInput && hiddenQtyInput) {
        if (btnMinus) {

            btnMinus.addEventListener('click', function() {
                let val = parseInt(qtyInput.value) || 1;
                if (val > 1) {
                    val--;
                    qtyInput.value = val;
                    hiddenQtyInput.value = val;
                }
            });
        }

        if (btnPlus) {
            btnPlus.addEventListener('click', function() {
                let val = parseInt(qtyInput.value) || 1;
                const max = parseInt(qtyInput.max);
                if (!Number.isNaN(max) && val >= max) {
                    return;
                }
                val++;
                qtyInput.value = val;
                hiddenQtyInput.value = val;
            });
        }

        qtyInput.addEventListener('change', function() {
            let val = parseInt(qtyInput.value) || 1;
            const max = parseInt(qtyInput.max);
            if (val < 1) val = 1;
            if (!Number.isNaN(max) && val > max) val = max;
            qtyInput.value = val;
            hiddenQtyInput.value = val;
        });
    }

    // 2. Checkout Payment Method Toggle
    const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
    const demoCardBlock = document.getElementById('demo-card-block');

    if (paymentRadios.length > 0 && demoCardBlock) {
        function togglePaymentFields() {
            let selectedVal = "";
            paymentRadios.forEach(function(radio) {
                if (radio.checked) {
                    selectedVal = radio.value;
                }
            });

            if (selectedVal === 'ONLINE') {
                demoCardBlock.style.display = 'block';
                // Mark demo fields as required
                setDemoFieldsRequired(true);
            } else {
                demoCardBlock.style.display = 'none';
                setDemoFieldsRequired(false);
            }
        }

        function setDemoFieldsRequired(req) {
            const cardFields = demoCardBlock.querySelectorAll('input');
            cardFields.forEach(function(field) {
                if (req) {
                    field.setAttribute('required', 'required');
                } else {
                    field.removeAttribute('required');
                }
            });
        }

        paymentRadios.forEach(function(radio) {
            radio.addEventListener('change', togglePaymentFields);
        });

        // Initialize state
        togglePaymentFields();
    }
});

// Update cart quantity submit helper (for cart table change buttons)
function submitQtyUpdate(cartItemId, newQty) {
    if (newQty < 1) {
        return;
    }

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/cart/update';

    // Inject CSRF token if present
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    if (csrfToken) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken.content;
        form.appendChild(csrfInput);
    }

    const itemInput = document.createElement('input');
    itemInput.type = 'hidden';
    itemInput.name = 'cartItemId';
    itemInput.value = cartItemId;
    form.appendChild(itemInput);

    const qtyInput = document.createElement('input');
    qtyInput.type = 'hidden';
    qtyInput.name = 'quantity';
    qtyInput.value = newQty;
    form.appendChild(qtyInput);

    document.body.appendChild(form);
    form.submit();
}

