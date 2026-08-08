const {z} = require('zod');

/**
 * The schemas for `fynancials.config.json`, which is user-editable input from outside the program. This module is the
 * single definition of every shape read from that file: zod validates at runtime and the static types are inferred
 * from the very same schemas, so a parsed shape can never drift from the code that describes it.
 *
 * The semantic checks do not live here - zod answers "is this shaped like a scrypt record", while the resource bounds
 * that keep `scryptSync` from becoming a memory or CPU bomb belong next to the call they protect (see `auth.js`).
 */

const scryptRecordSchema = z.strictObject({
  salt: z.base64(),
  hash: z.base64(),
  cost: z.int().positive(),
  blockSize: z.int().positive(),
  parallelization: z.int().positive()
});

// strict on both variants is what makes the union decide by key rather than by heuristic: an entry carrying both keys
// matches neither, instead of silently reading as a scrypt record
const authEntrySchema = z.union([
  z.strictObject({scrypt: scryptRecordSchema}),
  z.strictObject({passwordless: z.literal(true)})
]);

const fynancialsConfigSchema = z.looseObject({
  // `catchall` keeps arbitrary user-added environment entries valid while FY_DB_FILE_PATH stays a declared key, which
  // is what makes `config.env.FY_DB_FILE_PATH` legal under `noPropertyAccessFromIndexSignature`
  env: z.object({FY_DB_FILE_PATH: z.string().optional()}).catchall(z.string()),
  // deliberately not validated per `authEntrySchema` here: a single mangled entry must make *that* database pending
  // (`authStateOf` classifies it) rather than throwing away the whole config file
  auth: z.record(z.string(), z.unknown()).default({}),
  // one-shot: read at start to force `configure` mode and deleted in the same step (epic ADR-006), so it can never
  // be left dangling by any way of leaving the configuration screen
  configureOnNextStart: z.boolean().optional()
});

/** @typedef {import('zod').infer<typeof scryptRecordSchema>} ScryptRecord */
/** @typedef {import('zod').infer<typeof authEntrySchema>} AuthEntry */
/** @typedef {import('zod').infer<typeof fynancialsConfigSchema>} FynancialsConfig */

module.exports = {
  scryptRecordSchema,
  authEntrySchema,
  fynancialsConfigSchema
};
