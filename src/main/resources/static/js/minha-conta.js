// Função para atualizar os dados do perfil
async function atualizarPerfil() {
    // Pegar todos os valores do formulário
    const form = document.getElementById('profileForm');
    const formData = new FormData(form);

    // Criar objeto com os dados
    const dados = {
        nome: formData.get('nome'),
        telefone: formData.get('telefone'),
        senha: formData.get('novaSenha') || null
    };

    try {
        const response = await fetch('/usuario/minha-conta/atualizar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...cabecalhoCsrf()
            },
            body: JSON.stringify(dados)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.erro || 'Erro ao atualizar dados');
        }

        return await response.json();

    } catch (error) {
        console.error('Erro:', error);
        throw error;
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const editBtn = document.getElementById('editBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    const profileForm = document.getElementById('profileForm');
    const passwordFields = document.getElementById('passwordFields');

    // Botão de editar perfil
    editBtn.addEventListener('click', function () {
        const editableInputs = document.querySelectorAll('#profileForm input:not([data-no-edit])');
        editableInputs.forEach(input => {
            input.removeAttribute('readonly');
            input.removeAttribute('disabled');
        });

        passwordFields.classList.remove('d-none');
        const senhaInputs = passwordFields.querySelectorAll('input');
        senhaInputs.forEach(input => {
            input.removeAttribute('disabled');
        });

        this.classList.add('d-none');
        document.getElementById('saveBtn').classList.remove('d-none');
        document.getElementById('cancelBtn').classList.remove('d-none');
    });

    // Botão de cancelar edição
    cancelBtn.addEventListener('click', function () {
        const editableInputs = document.querySelectorAll('#profileForm input:not([data-no-edit])');
        editableInputs.forEach(input => {
            input.setAttribute('readonly', true);
            input.setAttribute('disabled', true);
        });

        passwordFields.classList.add('d-none');
        const senhaInputs = passwordFields.querySelectorAll('input');
        senhaInputs.forEach(input => {
            input.value = '';
            input.setAttribute('disabled', true);
        });

        document.getElementById('editBtn').classList.remove('d-none');
        document.getElementById('saveBtn').classList.add('d-none');
        this.classList.add('d-none');
    });

    // Submissão do formulário
    profileForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const novaSenha = document.getElementById('novaSenha').value;
        const confirmarSenha = document.getElementById('confirmarSenha').value;

        if (novaSenha || confirmarSenha) {
            if (novaSenha !== confirmarSenha) {
                alert('As senhas não coincidem!');
                return;
            }
        }

        try {
            await atualizarPerfil();
            alert('Dados atualizados com sucesso!');

            const editableInputs = document.querySelectorAll('#profileForm input:not([data-no-edit])');
            editableInputs.forEach(input => {
                input.setAttribute('readonly', true);
                input.setAttribute('disabled', true);
            });

            passwordFields.classList.add('d-none');
            document.getElementById('novaSenha').value = '';
            document.getElementById('confirmarSenha').value = '';

            document.getElementById('editBtn').classList.remove('d-none');
            document.getElementById('saveBtn').classList.add('d-none');
            document.getElementById('cancelBtn').classList.add('d-none');

        } catch (error) {
            alert('Erro ao atualizar dados. Tente novamente.');
        }
    });
});

$(document).ready(function () {
    // Máscara do CPF
    $('#cpf').mask('999.999.999-99');

    // Máscara do telefone
    $('#telefone').mask('(99) 99999-9999');
});

function formatarNome(nomeCompleto) {

    let partes = nomeCompleto.trim().split(/\s+/);
    if (partes.length === 1) {
        return partes[0];
    }

    let primeiroNome = partes[0];
    let ultimoNome = partes[partes.length - 1];
    return `${primeiroNome} ${ultimoNome}`;
}

function atualizarNomeUsuario() {

    let spanNome = document.querySelector('span#nome');
    if (spanNome) {
        let nomeCompleto = spanNome.textContent;
        let nomeFormatado = formatarNome(nomeCompleto);
        spanNome.textContent = nomeFormatado;
    }
}
document.addEventListener('DOMContentLoaded', atualizarNomeUsuario);

