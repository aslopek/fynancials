const {z} = require('zod');

/**
 * The renderer is outside the main process, so any argument arriving over IPC falls under the zod boundary rule
 * (see `../LLM.md`), exactly like disk or network input. This module is the home for that input's schemas.
 */

const backendStartPasswordSchema = z.string().optional();

module.exports = {backendStartPasswordSchema};
