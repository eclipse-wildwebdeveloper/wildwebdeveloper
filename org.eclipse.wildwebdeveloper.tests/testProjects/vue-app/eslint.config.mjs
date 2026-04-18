import vue from 'eslint-plugin-vue';
import js from '@eslint/js';

export default [
	js.configs.recommended,
	...vue.configs['flat/essential'],
];