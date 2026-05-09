document.addEventListener('DOMContentLoaded', function() {
    
    // --- Theme Toggle logic ---
    const initTheme = () => {
        const themeToggle = document.getElementById('themeToggle');
        const htmlElement = document.documentElement;
        
        const applyTheme = (theme) => {
            if (theme === 'light-theme') {
                htmlElement.classList.add('light-theme');
            } else {
                htmlElement.classList.remove('light-theme');
            }
        };

        const savedTheme = localStorage.getItem('theme');
        if (savedTheme) {
            applyTheme(savedTheme);
        }

        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                const isLight = htmlElement.classList.contains('light-theme');
                const newTheme = isLight ? '' : 'light-theme';
                applyTheme(newTheme);
                localStorage.setItem('theme', newTheme);
            });
        }
    };

    try {
        initTheme();
    } catch (e) {
        console.error('Theme toggle init failed', e);
    }

    // --- Search functionality ---
    const allSearchBtns = document.querySelectorAll('.searchBtn');
    const searchBar = document.querySelector('.searchBar');
    const searchInput = document.getElementById('searchInput');
    const searchClose = document.getElementById('searchClose');

    if (searchBar && allSearchBtns.length > 0) {
        const openSearch = () => {
            searchBar.classList.add('open');
            allSearchBtns.forEach(btn => btn.setAttribute('aria-expanded', 'true'));
            setTimeout(() => searchInput.focus(), 100);
        };

        const closeSearch = () => {
            searchBar.classList.remove('open');
            allSearchBtns.forEach(btn => btn.setAttribute('aria-expanded', 'false'));
        };

        allSearchBtns.forEach(btn => {
            btn.addEventListener('click', openSearch);
        });

        if (searchClose) {
            searchClose.addEventListener('click', closeSearch);
        }

        // Close on ESC key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && searchBar.classList.contains('open')) {
                closeSearch();
            }
        });
    }

    // --- Code Blocks Enhancement ---
    const enhanceCodeBlocks = () => {
        const codeBlocks = document.querySelectorAll('pre');
        
        codeBlocks.forEach(block => {
            if (block.parentElement.classList.contains('code-window')) return;
            
            const codeTag = block.querySelector('code');
            if (!codeTag) return;

            let language = 'code';
            const classes = Array.from(codeTag.classList);
            const langClass = classes.find(c => c.startsWith('language-'));
            
            if (langClass) {
                language = langClass.replace('language-', '');
            }

            block.classList.add('line-numbers');
            
            const wrapper = document.createElement('div');
            wrapper.className = 'code-window';
            
            const header = document.createElement('div');
            header.className = 'code-header';
            
            header.innerHTML = `
                <div class="code-controls">
                    <div class="code-dot code-dot--red"></div>
                    <div class="code-dot code-dot--yellow"></div>
                    <div class="code-dot code-dot--green"></div>
                </div>
                <div class="code-meta">
                    <span class="code-lang">${language}</span>
                    <button class="code-copy">
                        <i class="bi bi-clipboard"></i>
                        <span>Copy</span>
                    </button>
                </div>
            `;
            
            block.parentNode.insertBefore(wrapper, block);
            wrapper.appendChild(header);
            wrapper.appendChild(block);
            
            const copyBtn = header.querySelector('.code-copy');
            copyBtn.addEventListener('click', () => {
                const code = codeTag.innerText;
                navigator.clipboard.writeText(code).then(() => {
                    copyBtn.classList.add('copied');
                    copyBtn.querySelector('span').innerText = 'Copied!';
                    copyBtn.querySelector('i').className = 'bi bi-check2';
                    
                    setTimeout(() => {
                        copyBtn.classList.remove('copied');
                        copyBtn.querySelector('span').innerText = 'Copy';
                        copyBtn.querySelector('i').className = 'bi bi-clipboard';
                    }, 2000);
                });
            });
        });

        if (typeof Prism !== 'undefined') {
            Prism.highlightAll();
        }
    };

    enhanceCodeBlocks();
    setTimeout(enhanceCodeBlocks, 500);
});
