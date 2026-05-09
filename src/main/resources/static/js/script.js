document.addEventListener('DOMContentLoaded', function() {
    
    const allSearchBtns = document.querySelectorAll('.searchBtn');
    const searchBar = document.querySelector('.searchBar');
    const searchInput = document.getElementById('searchInput');
    const searchClose = document.getElementById('searchClose');

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

    searchClose.addEventListener('click', closeSearch);

    // Close on ESC key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && searchBar.classList.contains('open')) {
            closeSearch();
        }
    });

    // --- Code Blocks Enhancement ---
    const enhanceCodeBlocks = () => {
        const codeBlocks = document.querySelectorAll('pre');
        
        codeBlocks.forEach(block => {
            // Avoid double wrapping
            if (block.parentElement.classList.contains('code-window')) return;
            
            const codeTag = block.querySelector('code');
            if (!codeTag) return;

            // Extract language from code class (e.g., language-java)
            let language = 'code';
            const classes = Array.from(codeTag.classList);
            const langClass = classes.find(c => c.startsWith('language-'));
            
            if (langClass) {
                language = langClass.replace('language-', '');
            }

            // Add Prism's line-numbers class to pre
            block.classList.add('line-numbers');
            
            // Create wrapper
            const wrapper = document.createElement('div');
            wrapper.className = 'code-window';
            
            // Create header
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
            
            // Wrap the block
            block.parentNode.insertBefore(wrapper, block);
            wrapper.appendChild(header);
            wrapper.appendChild(block);
            
            // Copy functionality
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

        // Tell Prism to highlight the new structure if it hasn't already
        if (typeof Prism !== 'undefined') {
            Prism.highlightAll();
        }
    };

    // Run enhancement
    enhanceCodeBlocks();
    
    // Also run after a short delay to account for Prism's async highlighting if any
    setTimeout(enhanceCodeBlocks, 500);
});
