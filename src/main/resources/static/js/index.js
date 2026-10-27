document.querySelectorAll('.btn.btn-primary.btn-reserve').forEach(button => {
    button.addEventListener('click', function () {

        let data = document.getElementById('data').value;
        let horaInicio = document.getElementById('horaInicio').value;
        let horaFim = document.getElementById('horaFim').value;
        if (!data || !horaInicio || !horaFim) {
            alert('Por favor, selecione a data e horários desejados.');
            return;
        }
        window.location.href = '/usuario/login';
    });
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


document.addEventListener('DOMContentLoaded', function () {
    const searchForm = document.getElementById('searchForm');
    const resultadosContainer = document.getElementById('resultados');

    searchForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const formData = new FormData(searchForm);
        const params = new URLSearchParams();

        formData.forEach((value, key) => {
            if (value) {
                if (key === 'comodidades') {
                    params.append(key, value);
                } else {
                    params.append(key, value);
                }
            }
        });

        try {
            const response = await fetch(`ambiente/buscar?${params.toString()}`);
            const textResponse = await response.text();
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const ambientes = JSON.parse(textResponse);
            atualizarListaAmbientes(ambientes);
        } catch (error) {
            console.error('Erro ao buscar ambientes:', error);
            alert('Erro ao buscar ambientes. Por favor, tente novamente.');
        }
    });

    function atualizarListaAmbientes(ambientes) {
        if (!resultadosContainer) {
            console.error('Container de resultados não encontrado');
            return;
        }

        resultadosContainer.innerHTML = '';

        if (ambientes.length === 0) {
            resultadosContainer.innerHTML = '<div class="col-12"><p class="text-center">Nenhum ambiente encontrado com os critérios selecionados.</p></div>';
            return;
        }

        ambientes.forEach(ambiente => {
            const card = document.createElement('div');
            card.className = 'col';

            const comodidadesHTML = ambiente.comodidades.map(comodidade => {
                const icone = getComodidadeIcone(comodidade);
                return `<span class="me-2" title="${comodidade}">${icone}</span>`;
            }).join('');

            card.innerHTML = `
            <div class="card room-card h-100 d-flex flex-column">
                <img src="/img/${ambiente.imagem_nome}" 
                     class="card-img-top" 
                     alt="${ambiente.nome}"
                     onerror="this.src='/images/ambientes/default.jpg'">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title">${ambiente.nome}</h5>
                    <p class="card-text">${ambiente.descricao || ''}</p>
                    <p class="card-amount">Capacidade: ${ambiente.capacidade} pessoas</p>
                    <div class="comodidades-container mb-3">
                        ${comodidadesHTML}
                    </div>
                    <div class="mt-auto">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="h5 mb-0">R$ ${ambiente.valorHora}/hora</span>
                            <button onclick="reservarAmbiente(${ambiente.id})" class="btn btn-primary">Reservar</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
            resultadosContainer.appendChild(card);
        });
    }

// Função auxiliar para retornar o ícone SVG correspondente a cada comodidade
    function getComodidadeIcone(comodidade) {
        switch (comodidade) {
            case 'PROJETOR':
                return '<i class="bi bi-projector me-1 comodidade" title="Projetor"></i>';
            case 'TV':
                return '<i class="bi bi-tv me-1 comodidade" title="Televisão"></i>';
            case 'AR_CONDICIONADO':
                return '<i class="bi bi-thermometer-half me-1 comodidade" title="Ar Condicionado"></i>';
            case 'QUADRO_BRANCO':
                return '<i class="bi bi-easel me-1 comodidade" title="Quadro Branco"></i>';
            case 'CAFE':
                return '<i class="bi bi-cup-hot me-1 comodidade" title="Café"></i>';
            default:
                return '';
        }
    }
});

function reservarAmbiente(ambienteId) {
    const data = document.getElementById('data').value;
    const horaInicio = document.getElementById('horaInicial').value;
    const horaFim = document.getElementById('horaFinal').value;

    if (!data || !horaInicio || !horaFim) {
        alert('Por favor, selecione a data e horários para sua reserva.');
        return;
    }

    // Formatar data e hora para o formato esperado pelo backend
    const dataFormatada = `${data}T00:00:00`;
    const horaInicioFormatada = `${data}T${horaInicio}:00`;
    const horaFimFormatada = `${data}T${horaFim}:00`;

    fetch('/usuario/status')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Erro ao verificar status do usuário');
                }
                return response.json();
            })
            .then(statusData => {
                if (!statusData[0]) {
                    window.location.href = '/usuario/login?redirect=' + encodeURIComponent(window.location.pathname);
                    throw new Error('redirect'); // Interrompe a cadeia de promises
                }

                const reservaData = {
                    ambienteId: ambienteId,
                    data: dataFormatada,
                    horaInicio: horaInicioFormatada,
                    horaFim: horaFimFormatada
                };

                return fetch('/reserva/criar', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        ...cabecalhoCsrf()
                    },
                    body: JSON.stringify(reservaData)
                });
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text || 'Erro ao processar a reserva');
                    });
                }
                return response.json();
            })
            .then(reserva => {
                Swal.fire({
                    title: 'Reserva Confirmada!',
                    text: `Sua reserva foi realizada com sucesso para o dia ${dateFns.format(dateFns.parseISO(data), "dd/MM/yyyy")} das ${horaInicio} às ${horaFim}.`,
                    icon: 'success',
                    confirmButtonText: 'Ver Minhas Reservas',
                    showCancelButton: true,
                    cancelButtonText: 'Continuar Navegando'
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = '/usuario/minhas-reservas';
                    } else {
                        document.getElementById('searchForm').dispatchEvent(new Event('submit'));
                    }
                });
            })
            .catch(error => {
                if (error.message !== 'redirect') {
                    console.error('Erro:', error);
                    Swal.fire({
                        title: 'Erro!',
                        text: error.message || 'Não foi possível realizar a reserva. Por favor, tente novamente.',
                        icon: 'error',
                        confirmButtonText: 'Ok'
                    });
                }
            });
}

document.addEventListener('DOMContentLoaded', function () {
    const horaInicial = document.getElementById('horaInicial');
    const horaFinal = document.getElementById('horaFinal');

    // Atualizar hora final quando hora inicial mudar
    horaInicial.addEventListener('change', function () {
        const horaInicialValue = this.value;
        const options = horaFinal.options;

        // Habilitar/desabilitar opções da hora final
        for (let i = 0; i < options.length; i++) {
            const horaFinalValue = options[i].value;
            options[i].disabled = horaFinalValue <= horaInicialValue;

            // Selecionar próximo horário disponível se atual estiver desabilitado
            if (horaFinal.value <= horaInicialValue) {
                const proximaOpcao = Array.from(options).find(option =>
                    option.value > horaInicialValue
                );
                if (proximaOpcao) {
                    horaFinal.value = proximaOpcao.value;
                }
            }
        }
    });

    horaInicial.dispatchEvent(new Event('change'));
});


document.addEventListener('DOMContentLoaded', function () {
    const dataInput = document.getElementById('data');

    // Configurar data mínima (hoje)
    const hoje = new Date();
    const dataMinima = hoje.toISOString().split('T')[0];

    // Configurar data máxima (3 meses a partir de hoje)
    const tresMesesDepois = new Date();
    tresMesesDepois.setMonth(tresMesesDepois.getMonth() + 3);
    const dataMaxima = tresMesesDepois.toISOString().split('T')[0];

    // Definir atributos min e max
    dataInput.setAttribute('min', dataMinima);
    dataInput.setAttribute('max', dataMaxima);

    // Definir data padrão como hoje
    dataInput.value = dataMinima;

    // Validação adicional quando o valor mudar
    dataInput.addEventListener('change', function () {
        const [ano, mes, dia] = this.value.split('-');
        const dataSemHorario = new Date(ano, mes - 1, dia).toString();
        const dataBase = dataSemHorario.split('00:00:00')[0];

        const dataAtual = new Date().toString();
        const anoAtual = dataAtual.match(/\d{4}/)[0];
        const horarioAtual = dataAtual.split(anoAtual)[1].trim();

        const dataSelecionada = new Date(dataBase + horarioAtual);

        if (dataSelecionada < hoje) {
            alert('Por favor, selecione uma data a partir de hoje');
            this.value = dataMinima;
        } else if (dataSelecionada > tresMesesDepois) {
            alert('Por favor, selecione uma data dentro dos próximos 3 meses');
            this.value = dataMaxima;
        }
    });

    // Validar data quando o formulário for submetido
    const form = document.getElementById('searchForm');
    form.addEventListener('submit', function (event) {
        const [ano, mes, dia] = dataInput.value.split('-');
        const dataSemHorario = new Date(ano, mes - 1, dia).toString();
        const dataBase = dataSemHorario.split('00:00:00')[0];

        const dataAtual = new Date().toString();
        const anoAtual = dataAtual.match(/\d{4}/)[0];
        const horarioAtual = dataAtual.split(anoAtual)[1].trim();

        const dataSelecionada = new Date(dataBase + horarioAtual);

        if (dataSelecionada < hoje || dataSelecionada > tresMesesDepois) {
            event.preventDefault();
            alert('Por favor, selecione uma data válida entre hoje e os próximos 3 meses');
        }
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const dataInput = document.getElementById('data');
    const horaInicial = document.getElementById('horaInicial');
    const horaFinal = document.getElementById('horaFinal');

    function desabilitarHorariosPassados() {

        const [ano, mes, dia] = dataInput.value.split('-');
        const dataSemHorario = new Date(ano, mes - 1, dia).toString();
        const dataBase = dataSemHorario.split('00:00:00')[0];

        const dataAtual = new Date().toString();
        const anoAtual = dataAtual.match(/\d{4}/)[0];
        const horarioAtual = dataAtual.split(anoAtual)[1].trim();

        const dataSelecionada = new Date(dataBase + horarioAtual);
        const hoje = new Date();
        const horaAtual = hoje.getHours();
        const minutosAtual = hoje.getMinutes();

        // Só aplica a lógica se a data selecionada for hoje
        if (dataSelecionada.toDateString() === hoje.toDateString()) {
            Array.from(horaInicial.options).forEach(option => {
                const [hora, minutos] = option.value.split(':').map(Number);

                // Desabilita opções de horário que já passaram
                if (hora < horaAtual || (hora === horaAtual && minutos <= minutosAtual)) {
                    option.disabled = true;
                } else {
                    option.disabled = false;
                }
            });

            // Se o horário atual selecionado está desabilitado, seleciona o próximo disponível
            if (horaInicial.selectedOptions[0].disabled) {
                const proximoHorarioDisponivel = Array.from(horaInicial.options)
                        .find(option => !option.disabled);

                if (proximoHorarioDisponivel) {
                    horaInicial.value = proximoHorarioDisponivel.value;
                }
            }
        } else {
            // Se não for hoje, habilita todos os horários
            Array.from(horaInicial.options).forEach(option => {
                option.disabled = false;
            });
        }

        // Atualiza as opções do horário final
        atualizarHoraFinal();
    }

    function atualizarHoraFinal() {
        const horaInicialValue = horaInicial.value;
        const options = horaFinal.options;

        // Habilitar/desabilitar opções da hora final
        for (let i = 0; i < options.length; i++) {
            const horaFinalValue = options[i].value;
            options[i].disabled = horaFinalValue <= horaInicialValue;

            // Selecionar próximo horário disponível se atual estiver desabilitado
            if (horaFinal.value <= horaInicialValue) {
                const proximaOpcao = Array.from(options).find(option =>
                    option.value > horaInicialValue && !option.disabled
                );
                if (proximaOpcao) {
                    horaFinal.value = proximaOpcao.value;
                }
            }
        }
    }

    // Event listeners
    dataInput.addEventListener('change', desabilitarHorariosPassados);
    horaInicial.addEventListener('change', atualizarHoraFinal);

    // Executar na inicialização
    desabilitarHorariosPassados();
});