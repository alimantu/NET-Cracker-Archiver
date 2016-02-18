package com.netcracker.edu.salinskii.archiver;

import java.util.Arrays;

/**
 * Created by Alimantu on 22/11/15.
 */
public class ArchiverMain {

    public static void main(String[] args) throws IllegalAccessException {
        if (args.length < 2) {
            printFormat();
            System.exit(0);
        }
        Archiver archiver = new Archiver();
        String command = args[0].toLowerCase();
        switch (command) {
            case "compress":
                if (args.length < 4) {
                    printFormat();
                } else {
                    archiver.addFilesToArchive(args[1], args[2], Arrays.copyOfRange(args, 3, args.length));
                }
                break;
            case "uncompress":
                if (args.length >= 3) {
                    archiver.getFilesFromArchive(args[1], args[2]);
                } else {
                    archiver.getFilesFromArchive(args[1]);
                }
                break;
            case "comment":
                if (args.length == 3) {
                    archiver.setComment(args[1], args[2]);
                } else {
                    printFormat();
                }
                break;
            case "getcomment":
                System.out.println(archiver.getComment(args[1]));
                break;
            default:
                printFormat();
                break;
        }

    }

    private static void printFormat() {
        System.out.println("Bad input, correct format: \n(Compress <Destination File> <Comment> <Files to compress>+)\n" +
                "| (Uncompress <Source File> <Destination dir>?)\n" +
                "| (Comment <Destination File> <Comment>)\n" +
                "| (GetComment <Source File>)\n" +
                "All inputs must be without brackets!");
    }


}
