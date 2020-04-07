const btnRunJavaScript = document.getElementById('btnRunJavaScript');
const btnRunMiniJava = document.getElementById('btnRunMiniJava');

function disableButtons() {
    btnRunJavaScript.setAttribute('disabled', 'disabled');
    btnRunMiniJava.setAttribute('disabled', 'disabled');
}

btnRunJavaScript.addEventListener('click', () => {
    disableButtons();
    btnRunMiniJava.classList.remove('btn-primary');
    btnRunMiniJava.classList.add('btn-secondary');
    require('./js-version')();
});

btnRunMiniJava.addEventListener('click', () => {
    disableButtons();
    btnRunJavaScript.classList.remove('btn-primary');
    btnRunJavaScript.classList.add('btn-secondary');
    require('./wasm-version')();
});