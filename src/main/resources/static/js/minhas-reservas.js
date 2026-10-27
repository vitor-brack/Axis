function cancelarReserva(id) {
    if (confirm('Tem certeza que deseja cancelar esta reserva?')) {
        fetch(`/usuario/minhas-reservas/${id}/cancelar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...cabecalhoCsrf()
            },
            credentials: 'include'
        })
                .then(async response => {
                    if (response.ok) {
                        window.location.reload();
                    } else {
                        const error = await response.json();
                        alert(error.erro || 'Erro ao cancelar reserva');
                    }
                })
                .catch(error => {
                    console.error('Erro:', error);
                    alert('Erro ao cancelar reserva');
                });
    }
}

function editarReserva(id) {
    // Preencher o modal com os dados da reserva
    document.getElementById('reservaId').value = id;

    // Abrir o modal
    new bootstrap.Modal(document.getElementById('editarReservaModal')).show();
}

function salvarAlteracoes() {
    const id = document.getElementById('reservaId').value;
    const novaData = document.getElementById('novaData').value;
    const novoInicio = document.getElementById('novoHorarioInicio').value;
    const novoFim = document.getElementById('novoHorarioFim').value;

    fetch(`/usuario/minhas-reservas/${id}/alterar`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...cabecalhoCsrf()
        },
        body: JSON.stringify({
            data: novaData,
            horaInicio: novoInicio,
            horaFim: novoFim
        })
    })
            .then(async response => {
                if (response.ok) {
                    window.location.reload();
                } else {
                    const errorData = await response.json();
                    // Exibe mensagem de erro específica ou mensagem padrão
                    alert(errorData.erro || 'Erro ao alterar reserva');
                }
            })
            .catch(error => {
                console.error('Erro:', error);
                alert('Erro ao alterar reserva');
            });
}

document.addEventListener('DOMContentLoaded', function () {
    const novaData = document.getElementById('novaData');
    const novoHorarioInicio = document.getElementById('novoHorarioInicio');
    const novoHorarioFim = document.getElementById('novoHorarioFim');

    // Configurar data mínima (hoje) e máxima (3 meses)
    const hoje = new Date();
    const dataMinima = hoje.toISOString().split('T')[0];

    const tresMesesDepois = new Date();
    tresMesesDepois.setMonth(tresMesesDepois.getMonth() + 3);
    const dataMaxima = tresMesesDepois.toISOString().split('T')[0];

    novaData.setAttribute('min', dataMinima);
    novaData.setAttribute('max', dataMaxima);

    // Validação da hora final quando hora inicial mudar
    novoHorarioInicio.addEventListener('change', function () {
        const horaInicialValue = this.value;
        const options = novoHorarioFim.options;

        for (let i = 0; i < options.length; i++) {
            const horaFinalValue = options[i].value;
            options[i].disabled = horaFinalValue <= horaInicialValue;

            if (novoHorarioFim.value <= horaInicialValue) {
                const proximaOpcao = Array.from(options).find(option =>
                    option.value > horaInicialValue && !option.disabled
                );
                if (proximaOpcao) {
                    novoHorarioFim.value = proximaOpcao.value;
                }
            }
        }
    });

    // Validação de horários passados
    function desabilitarHorariosPassados() {
        const [ano, mes, dia] = novaData.value.split('-');
        const dataSelecionada = new Date(ano, mes - 1, dia);
        const hoje = new Date();

        if (dataSelecionada.toDateString() === hoje.toDateString()) {
            const horaAtual = hoje.getHours();
            const minutosAtual = hoje.getMinutes();

            Array.from(novoHorarioInicio.options).forEach(option => {
                const [hora, minutos] = option.value.split(':').map(Number);
                option.disabled = hora < horaAtual || (hora === horaAtual && minutos <= minutosAtual);
            });

            if (novoHorarioInicio.selectedOptions[0].disabled) {
                const proximoHorarioDisponivel = Array.from(novoHorarioInicio.options)
                        .find(option => !option.disabled);

                if (proximoHorarioDisponivel) {
                    novoHorarioInicio.value = proximoHorarioDisponivel.value;
                    novoHorarioInicio.dispatchEvent(new Event('change'));
                }
            }
        } else {
            Array.from(novoHorarioInicio.options).forEach(option => {
                option.disabled = false;
            });
        }
    }

    novaData.addEventListener('change', desabilitarHorariosPassados);
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

