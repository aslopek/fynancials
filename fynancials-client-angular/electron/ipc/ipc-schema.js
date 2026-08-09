const {z} = require('zod');

/**
 * The renderer is outside the main process, so any argument arriving over IPC falls under the zod boundary rule
 * (see `../LLM.md`), exactly like disk or network input. This module is the home for that input's schemas.
 */

const backendStartPasswordSchema = z.string().optional();
const authVerifyPasswordSchema = z.string();

const databasePathSchema = z.string().min(1);

const databaseSelectionSchema = databasePathSchema.nullable();

// strictObject, so story #38 adds its `java` key here rather than loosening what a section may smuggle through
const configurationChangesSchema = z.strictObject({databasePath: databasePathSchema});

module.exports = {
  backendStartPasswordSchema,
  authVerifyPasswordSchema,
  databasePathSchema,
  databaseSelectionSchema,
  configurationChangesSchema
};
