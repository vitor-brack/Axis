// Header com o token CSRF que o layout coloca no <head> (fragmentos/layout.html)
function cabecalhoCsrf() {
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    return { [header]: token };
}
