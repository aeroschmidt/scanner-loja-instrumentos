const grid = document.querySelector('#product-grid');
const dialog = document.querySelector('#product-dialog');
const form = document.querySelector('#product-form');
const statusMessage = document.querySelector('#status');
const api = '/api/products';
const icons = ['♫', '🎸', '🎹', '🎤', '🎧', '♬'];

async function request(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const detail = await response.text();
        throw new Error(detail || `Falha na solicitação (${response.status})`);
    }
    return response.status === 204 ? null : response.json();
}

function showStatus(message, isError = false) {
    statusMessage.textContent = message;
    statusMessage.classList.toggle('error', isError);
}

function renderProducts(products) {
    grid.replaceChildren();
    if (products.length === 0) {
        const empty = document.createElement('p');
        empty.textContent = 'Ainda não há itens cadastrados. Adicione o primeiro instrumento.';
        grid.append(empty);
        return;
    }

    products.forEach((product, index) => {
        const card = document.createElement('article');
        card.className = 'product-card';
        const art = document.createElement('div');
        art.className = 'card-art';
        art.setAttribute('aria-hidden', 'true');
        art.textContent = icons[index % icons.length];
        const content = document.createElement('div');
        content.className = 'card-content';
        const category = document.createElement('p');
        category.className = 'category';
        category.textContent = product.category;
        const name = document.createElement('h3');
        name.className = 'product-name';
        name.textContent = product.name;
        const bottom = document.createElement('div');
        bottom.className = 'card-bottom';
        const price = document.createElement('span');
        price.className = 'price';
        price.textContent = Number(product.price).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
        const stock = document.createElement('span');
        stock.className = 'stock';
        stock.textContent = `${product.stock} un.`;
        bottom.append(price, stock);
        const actions = document.createElement('div');
        actions.className = 'card-actions';
        const edit = document.createElement('button');
        edit.className = 'text-button';
        edit.type = 'button';
        edit.textContent = 'Editar';
        edit.addEventListener('click', () => openEditor(product));
        const remove = document.createElement('button');
        remove.className = 'text-button delete';
        remove.type = 'button';
        remove.textContent = 'Remover';
        remove.addEventListener('click', () => removeProduct(product.id));
        actions.append(edit, remove);
        content.append(category, name, bottom, actions);
        card.append(art, content);
        grid.append(card);
    });
}

async function loadProducts() {
    try {
        renderProducts(await request(api));
        showStatus('');
    } catch (error) {
        showStatus(`Não foi possível carregar o catálogo: ${error.message}`, true);
    }
}

function openEditor(product) {
    form.reset();
    document.querySelector('#dialog-title').textContent = product ? 'Editar instrumento' : 'Novo instrumento';
    document.querySelector('#product-id').value = product?.id ?? '';
    document.querySelector('#name').value = product?.name ?? '';
    document.querySelector('#category').value = product?.category ?? '';
    document.querySelector('#price').value = product?.price ?? '';
    document.querySelector('#stock').value = product?.stock ?? '';
    dialog.showModal();
}

async function removeProduct(id) {
    if (!window.confirm('Remover este item do catálogo?')) return;
    try {
        await request(`${api}/${id}`, { method: 'DELETE' });
        showStatus('Item removido.');
        await loadProducts();
    } catch (error) {
        showStatus(`Não foi possível remover o item: ${error.message}`, true);
    }
}

document.querySelector('#new-product').addEventListener('click', () => openEditor(null));
document.querySelector('#close-dialog').addEventListener('click', () => dialog.close());
document.querySelector('#cancel-dialog').addEventListener('click', () => dialog.close());
form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = document.querySelector('#product-id').value;
    const payload = Object.fromEntries(new FormData(form).entries());
    delete payload.id;
    payload.price = Number(payload.price);
    payload.stock = Number(payload.stock);
    try {
        await request(id ? `${api}/${id}` : api, {
            method: id ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        dialog.close();
        showStatus(id ? 'Item atualizado.' : 'Item adicionado.');
        await loadProducts();
    } catch (error) {
        showStatus(`Não foi possível salvar o item: ${error.message}`, true);
    }
});

loadProducts();
