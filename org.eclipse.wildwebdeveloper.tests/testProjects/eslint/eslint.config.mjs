import tsParser from '@typescript-eslint/parser';

export default [
	{
		rules: {
			indent: 'error',
		},
	},
	{
		files: ['**/*.ts', '**/*.tsx', '**/*.js', '**/*.jsx'],
		languageOptions: {
			parser: tsParser,
		},
	},
];