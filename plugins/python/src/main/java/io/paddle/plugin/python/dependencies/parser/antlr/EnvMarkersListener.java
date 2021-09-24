package io.paddle.plugin.python.dependencies.parser.antlr;

// Generated from EnvMarkers.g4 by ANTLR 4.8

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EnvMarkersParser}.
 */
public interface EnvMarkersListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#markerOp}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerOp(EnvMarkersParser.MarkerOpContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#markerOp}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerOp(EnvMarkersParser.MarkerOpContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#envVar}.
     *
     * @param ctx the parse tree
     */
    void enterEnvVar(EnvMarkersParser.EnvVarContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#envVar}.
     *
     * @param ctx the parse tree
     */
    void exitEnvVar(EnvMarkersParser.EnvVarContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#markerVar}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerVar(EnvMarkersParser.MarkerVarContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#markerVar}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerVar(EnvMarkersParser.MarkerVarContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#markerExpr}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerExpr(EnvMarkersParser.MarkerExprContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#markerExpr}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerExpr(EnvMarkersParser.MarkerExprContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#markerAnd}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerAnd(EnvMarkersParser.MarkerAndContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#markerAnd}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerAnd(EnvMarkersParser.MarkerAndContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#markerOr}.
     *
     * @param ctx the parse tree
     */
    void enterMarkerOr(EnvMarkersParser.MarkerOrContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#markerOr}.
     *
     * @param ctx the parse tree
     */
    void exitMarkerOr(EnvMarkersParser.MarkerOrContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#marker}.
     *
     * @param ctx the parse tree
     */
    void enterMarker(EnvMarkersParser.MarkerContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#marker}.
     *
     * @param ctx the parse tree
     */
    void exitMarker(EnvMarkersParser.MarkerContext ctx);

    /**
     * Enter a parse tree produced by {@link EnvMarkersParser#quotedMarker}.
     *
     * @param ctx the parse tree
     */
    void enterQuotedMarker(EnvMarkersParser.QuotedMarkerContext ctx);

    /**
     * Exit a parse tree produced by {@link EnvMarkersParser#quotedMarker}.
     *
     * @param ctx the parse tree
     */
    void exitQuotedMarker(EnvMarkersParser.QuotedMarkerContext ctx);
}
