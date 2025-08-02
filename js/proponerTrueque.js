document.addEventListener('DOMContentLoaded', () => {
    const API_BASE_URL = 'http://54.87.124.61/api';
    let selectedProductId = null;
    let desiredProductId = null;
    let currentUser = null;

    // --- ELEMENTOS DEL DOM ---
    const desiredProductName = document.getElementById('nombreProducto');
    const desiredProductDesc = document.getElementById('descripcionProducto');
    const desiredProductOwner = document.getElementById('publicadoPor');
    const desiredProductImage = document.getElementById('imagenProducto');
    const userProductsContainer = document.getElementById('productos');
    const proposeTradeButton = document.getElementById('btnProponerTrueque');
    const commentBox = document.getElementById('comentario-box');
    const loadingDiv = document.getElementById('loading');
    const errorDiv = document.getElementById('error');

    // --- INICIALIZACIÓN ---
    async function init() {
        currentUser = getUserDataFromSession();
        if (!currentUser) {
            showMessage('Debes iniciar sesión para proponer un trueque.', 'error');
            setTimeout(() => window.location.href = 'login.html', 3000);
            return;
        }

        desiredProductId = getProductIdFromUrl();
        if (!desiredProductId) {
            showMessage('No se especificó un producto deseado.', 'error');
            return;
        }

        showLoading(true);
        try {
            await Promise.all([
                loadDesiredProduct(desiredProductId),
                loadUserProducts(currentUser.id)
            ]);
        } catch (error) {
            showMessage(`Error al cargar los datos: ${error.message}`, 'error');
        } finally {
            showLoading(false);
        }
    }

    // --- LÓGICA DE CARGA DE DATOS ---
    async function loadDesiredProduct(productId) {
        const product = await fetchAPI(`/products/${productId}`);
        desiredProductName.textContent = product.nombre;
        desiredProductDesc.textContent = product.descripcion;
        desiredProductOwner.textContent = `${product.usuarioNombre} ${product.usuarioApellido}`;
        if (product.imagen) {
            desiredProductImage.innerHTML = `<img src="${product.imagen}" alt="${product.nombre}">`;
        }
    }

    async function loadUserProducts(userId) {
        const products = await fetchAPI(`/products/user/${userId}`);
        if (products.length === 0) {
            userProductsContainer.innerHTML = '<p>No tienes productos disponibles para ofrecer.</p>';
            proposeTradeButton.disabled = true;
        } else {
            const productsHTML = products.map(product => `
                <div class="producto-ofrecido-item" data-product-id="${product.id}">
                    <img src="${product.imagen}" alt="${product.nombre}">
                    <p>${product.nombre}</p>
                </div>
            `).join('');
            userProductsContainer.innerHTML = productsHTML;
            addUserProductListeners();
        }
    }

    // --- MANEJO DE EVENTOS ---
    function addUserProductListeners() {
        document.querySelectorAll('.producto-ofrecido-item').forEach(item => {
            item.addEventListener('click', () => {
                // Desmarcar el anterior seleccionado
                const currentSelected = document.querySelector('.producto-ofrecido-item.selected');
                if (currentSelected) {
                    currentSelected.classList.remove('selected');
                }
                // Marcar el nuevo
                item.classList.add('selected');
                selectedProductId = item.dataset.productId;
            });
        });
    }

    proposeTradeButton.addEventListener('click', async () => {
        if (!selectedProductId) {
            showMessage('Debes seleccionar uno de tus productos para ofrecer.', 'warning');
            return;
        }

        const proposal = {
            producto_ofrecido_id: parseInt(selectedProductId, 10),
            producto_deseado_id: parseInt(desiredProductId, 10),
            usuario_oferente_id: currentUser.id,
            comentario: commentBox.value.trim()
        };

        proposeTradeButton.disabled = true;
        proposeTradeButton.textContent = 'Enviando...';

        try {
            const result = await postAPI('/trades/propose', proposal);
            showMessage('¡Propuesta de trueque enviada exitosamente!', 'success');
            setTimeout(() => window.location.href = 'ExplorarTrueque.html', 3000);
        } catch (error) {
            showMessage(`Error al enviar la propuesta: ${error.message}`, 'error');
        } finally {
            proposeTradeButton.disabled = false;
            proposeTradeButton.textContent = 'Proponer Trueque';
        }
    });

    // --- FUNCIONES DE UTILIDAD ---
    function getProductIdFromUrl() {
        const params = new URLSearchParams(window.location.search);
        return params.get('id');
    }

    function getUserDataFromSession() {
        try {
            const sessionData = localStorage.getItem('sesion');
            return sessionData ? JSON.parse(sessionData) : null;
        } catch (e) {
            return null;
        }
    }

    async function fetchAPI(endpoint) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`);
        if (!response.ok) {
            throw new Error('Error de red al obtener datos.');
        }
        const result = await response.json();
        if (!result.success) {
            throw new Error(result.message || 'Error en la respuesta de la API.');
        }
        return result.data.product || result.data.products;
    }

    async function postAPI(endpoint, body) {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Error desconocido' }));
            throw new Error(errorData.message);
        }
        return await response.json();
    }

    function showLoading(isLoading) {
        loadingDiv.style.display = isLoading ? 'block' : 'none';
    }

    function showMessage(text, type = 'info') {
        const messageDiv = document.getElementById('message');
        messageDiv.textContent = text;
        messageDiv.className = `message message-${type}`;
        messageDiv.style.display = 'block';
        setTimeout(() => messageDiv.style.display = 'none', 4000);
    }

    init();
});
