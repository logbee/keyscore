global['CSS'] = null;

const mock = () => {
    let storage = {};
    return {
        getItem: key => key in storage ? storage[key] : null,
        setItem: (key, value) => storage[key] = value || '',
        removeItem: key => delete storage[key],
        clear: () => storage = {},
    };
};

Object.defineProperty(window, 'localStorage', {value: mock()});
Object.defineProperty(window, 'sessionStorage', {value: mock()});
Object.defineProperty(document, 'doctype', {
    value: '<!DOCTYPE html>'
});


Object.defineProperty(window, 'matchMedia', {
    value: () => {
        return {
            matches: false,
            addListener: () => {},
            removeListener: () => {}
        };
    }
});

Object.defineProperty(window, 'getComputedStyle', {
    value: () => {
        return {
            getPropertyValue: (prop) => {
                return ''
            },
            display: 'none',
            appearance: ['-webkit-appearance']
        };
    }
});
/**
 * ISSUE: https://github.com/angular/material2/issues/7101
 * Workaround for JSDOM missing transform property
 */
Object.defineProperty(document.body.style, 'transform', {
    value: () => {
        return {
            enumerable: true,
            configurable: true,
        };
    },
});

