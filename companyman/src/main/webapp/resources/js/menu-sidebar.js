/**
 * Controle da sidebar (WEB-INF/template/layout.xhtml):
 *
 * 1) Colapsar/expandir a sidebar inteira:
 *    - Desktop (> 768px): o botão redondo encolhe/expande a largura da
 *      sidebar (classe "collapsed"); o conteúdo principal se ajusta
 *      automaticamente, já que o layout é flex. O ícone de seta do botão
 *      gira 180° para indicar a direção da próxima ação.
 *    - Mobile (<= 768px): a sidebar vira um painel sobreposto
 *      ("off-canvas"), escondido por padrão fora da tela (transform) e
 *      exibido por cima do conteúdo ao clicar no botão (classe
 *      "mobile-open"), com um backdrop semitransparente para fechar ao
 *      clicar fora dela.
 *
 * 2) Colapsar/expandir cada um dos três submenus (Cadastros, Operações,
 *    Sistema) individualmente: como o <p:menu> do PrimeFaces renderiza os
 *    cabeçalhos de submenu e seus itens como <li> irmãos dentro de uma
 *    única <ul> (não aninhados), o próprio script agrupa dinamicamente os
 *    itens de cada cabeçalho em um wrapper e alterna sua visibilidade com
 *    uma transição suave de max-height/opacity (ver .submenu-items-wrapper
 *    em app.css).
 */
(function () {
    function larguraMobile() {
        return window.innerWidth <= 768;
    }

    document.addEventListener('DOMContentLoaded', function () {
        inicializarToggleDaSidebar();
        inicializarSubmenusColapsaveis();
    });

    function inicializarToggleDaSidebar() {
        let botaoAlternar = document.getElementById('sidebarToggle');
        let sidebar = document.getElementById('appSidebar');
        let backdrop = document.getElementById('sidebarBackdrop');

        if (!botaoAlternar || !sidebar || !backdrop) {
            return;
        }

        // A seta do botão sempre reflete se a sidebar está oculta (aponta
        // para a direita, indicando "clique para abrir") ou visível
        // (aponta para a esquerda, indicando "clique para fechar") —
        // independentemente de a ocultação ser por colapso (desktop) ou
        // por estar fora da tela (mobile, off-canvas).
        function atualizarIconeBotao() {
            let sidebarOculta = larguraMobile()
                ? !sidebar.classList.contains('mobile-open')
                : sidebar.classList.contains('collapsed');

            botaoAlternar.classList.toggle('collapsed', sidebarOculta);
        }

        function fecharSidebarMobile() {
            sidebar.classList.remove('mobile-open');
            backdrop.classList.remove('visible');
            atualizarIconeBotao();
        }

        botaoAlternar.addEventListener('click', function () {
            if (larguraMobile()) {
                sidebar.classList.toggle('mobile-open');
                backdrop.classList.toggle('visible');
            } else {
                sidebar.classList.toggle('collapsed');
            }

            atualizarIconeBotao();
        });

        backdrop.addEventListener('click', fecharSidebarMobile);

        // Ao redimensionar para desktop, garante que o estado "aberto no
        // mobile" não fique preso caso a janela seja alargada novamente.
        window.addEventListener('resize', function () {
            if (!larguraMobile()) {
                sidebar.classList.remove('mobile-open');
                backdrop.classList.remove('visible');
            }
            atualizarIconeBotao();
        });

        atualizarIconeBotao();
    }

    function inicializarSubmenusColapsaveis() {
        let sidebar = document.getElementById('appSidebar');

        if (!sidebar) {
            return;
        }

        // Cabeçalhos de submenu renderizados pelo <p:menu> (o PrimeFaces usa
        // a classe "ui-widget-header" para o <li> de cabeçalho de cada
        // grupo, em versões recentes também "ui-submenu-header").
        let cabecalhos = sidebar.querySelectorAll(
            '.ui-menu .ui-widget-header, .ui-menu .ui-submenu-header',
        );

        cabecalhos.forEach(function (cabecalho) {
            agruparItensDoSubmenu(cabecalho);
        });
    }

    /**
     * Move os <li> de itens que vêm logo após o cabeçalho de um submenu
     * (até encontrar o próximo cabeçalho ou o fim da lista) para dentro de
     * um wrapper dedicado, e liga o clique no cabeçalho para colapsar ou
     * expandir esse wrapper.
     */
    function agruparItensDoSubmenu(cabecalho) {
        let itens = [];
        let proximoIrmao = cabecalho.nextElementSibling;

        while (proximoIrmao && !elmCabecalhoDeSubmenu(proximoIrmao)) {
            let atual = proximoIrmao;

            proximoIrmao = proximoIrmao.nextElementSibling;
            itens.push(atual);
        }

        if (itens.length === 0) {
            return;
        }

        let wrapper = document.createElement('li');
        wrapper.className = 'submenu-items-wrapper';

        let listaInterna = document.createElement('ul');

        listaInterna.className = 'ui-menu-list ui-helper-reset';

        itens.forEach(function (item) {
            listaInterna.appendChild(item);
        });

        wrapper.appendChild(listaInterna);

        cabecalho.insertAdjacentElement('afterend', wrapper);

        let iconeToggle = document.createElement('span');

        iconeToggle.className = 'pi pi-chevron-down submenu-toggle-icon';

        cabecalho.appendChild(iconeToggle);
        cabecalho.setAttribute('role', 'button');
        cabecalho.setAttribute('tabindex', '0');
        cabecalho.setAttribute('aria-expanded', 'true');

        function alternarSubmenu() {
            let colapsado = cabecalho.classList.toggle('submenu-collapsed');

            wrapper.classList.toggle('submenu-collapsed', colapsado);
            cabecalho.setAttribute('aria-expanded', String(!colapsado));
        }

        cabecalho.addEventListener('click', alternarSubmenu);
        cabecalho.addEventListener('keydown', function (evento) {
            if (evento.key === 'Enter' || evento.key === ' ') {
                evento.preventDefault();
                alternarSubmenu();
            }
        });
    }

    function elmCabecalhoDeSubmenu(elemento) {
        return (
            elemento.classList.contains('ui-widget-header') ||
            elemento.classList.contains('ui-submenu-header')
        );
    }
})();
