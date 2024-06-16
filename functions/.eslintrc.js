module.exports = {
  env: {
    node: true,
    es2021: true,
  },
  parserOptions: {
    ecmaVersion: 2018,
  },
  extends: [
    'eslint:recommended',
    'google',
    'plugin:node/recommended',
  ],
  rules: {
    // Agrega reglas específicas si lo necesitas
    'no-restricted-globals': ['error', 'name', 'length'],
    'prefer-arrow-callback': 'error',
    'quotes': ['error', 'double', {'allowTemplateLiterals': true}],
  },
  overrides: [
    {
      files: ['**/*.spec.js'], // Puedes ajustar los patrones de archivos aquí
      env: {
        mocha: true, // Si usas pruebas con Mocha
      },
      rules: {
        // Reglas específicas para pruebas si es necesario
      },
    },
  ],
  globals: {
    // Definiciones globales específicas aquí
    'exports': true,
    'require': true,
  },
};