import vue from 'eslint-plugin-vue';
import babelParser from '@babel/eslint-parser';
import js from '@eslint/js';

export default [
	js.configs.recommended,
	...vue.configs['flat/vue3-essential'],
	{
		languageOptions: {
			parserOptions: {
				parser: babelParser,
			},
		},
	},
];