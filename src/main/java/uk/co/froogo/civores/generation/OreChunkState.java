package uk.co.froogo.civores.generation;

/**
 * The current state of generation for OreChunks.
 */
public enum OreChunkState {
    /**
     * OreChunk currently being generated asynchronously.
     */
    GENERATING,

    /**
     * OreChunk generated, but not yet sent to the client.
     */
    GENERATED,

    /**
     * OreChunk generated and sent to the client.
     */
    SENT
}
